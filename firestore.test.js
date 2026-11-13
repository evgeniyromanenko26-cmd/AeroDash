const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp() {
  return new Date(Date.now() - 2000);
}

function validProfile(uid) {
  const ts = pastTimestamp();
  return {
    userId: uid,
    displayName: "AeroPlayer",
    selectedSkinId: "aqua_prism",
    selectedTrailId: "cyan_bubbles",
    unlockedSkins: ["aqua_prism", "lime_aero"],
    totalOrbs: 15,
    totalAttempts: 3,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validLevelProgress(levelId) {
  const ts = pastTimestamp();
  return {
    levelId,
    bestPercent: 64,
    completed: false,
    attempts: 5,
    coinsCollected: 2,
    bestTimeMs: 0,
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write user profiles or levelProgress", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("levelProgress").get());
});

test("Authenticated owner can create and update their own UserProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(profileRef.set(validProfile(ALICE_UID)));
  await assertSucceeds(
    profileRef.update({
      selectedSkinId: "lime_aero",
      totalOrbs: 25,
      updatedAt: pastTimestamp(),
    })
  );
});

test("Rejects shadow ghost fields and oversized strings on UserProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validProfile(ALICE_UID),
      isAdmin: true,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validProfile(ALICE_UID),
      displayName: "A".repeat(100),
    })
  );
});

test("Cross-user isolation: Bob cannot read or write Alice's profile or levelProgress", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("levelProgress").doc("aqua_meadow").set(validLevelProgress("aqua_meadow"))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ totalOrbs: 999, updatedAt: pastTimestamp() }));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("levelProgress").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("levelProgress").doc("aqua_meadow").set(validLevelProgress("aqua_meadow"))
  );
});

test("Authenticated owner can create, update, and list levelProgress with valid bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const levelRef = aliceDb.collection("users").doc(ALICE_UID).collection("levelProgress").doc("aqua_meadow");
  await assertSucceeds(levelRef.set(validLevelProgress("aqua_meadow")));
  await assertSucceeds(
    levelRef.update({
      bestPercent: 100,
      completed: true,
      coinsCollected: 3,
      bestTimeMs: 42100,
      updatedAt: pastTimestamp(),
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("levelProgress").get());
});

test("Rejects out-of-range bestPercent or coinsCollected on levelProgress", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("levelProgress").doc("aqua_meadow").set({
      ...validLevelProgress("aqua_meadow"),
      bestPercent: 150,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("levelProgress").doc("aqua_meadow").set({
      ...validLevelProgress("aqua_meadow"),
      coinsCollected: 5,
    })
  );
});
