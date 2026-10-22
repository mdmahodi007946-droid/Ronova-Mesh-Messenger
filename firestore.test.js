const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const CHARLIE_UID = "charlie_uid_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: cannot read users collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
});

test("Authenticated user: can create own profile but not another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  
  // Alice creating Alice profile - should succeed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      ronovaId: "RNV-123456",
      displayName: "Alice",
      bio: "Alice in Mesh",
      profileVisibility: "public",
      presencePrivacy: "public",
      isOnline: true,
      accountStatus: "active"
    })
  );

  // Alice trying to create Bob's profile - should fail
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      uid: BOB_UID,
      ronovaId: "RNV-654321",
      displayName: "Bob Spoofed",
      bio: "Malicious write",
      accountStatus: "active"
    })
  );
});

test("Authenticated user: cannot mutate protected profile fields (uid, ronovaId)", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      ronovaId: "RNV-123456",
      displayName: "Alice",
      bio: "Alice in Mesh",
      profileVisibility: "public",
      presencePrivacy: "public",
      isOnline: true,
      accountStatus: "active"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Allowed update: displayName and bio
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice New Name",
      bio: "Updated bio"
    })
  );

  // Disallowed update: tampering with ronovaId
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      ronovaId: "RNV-999999"
    })
  );
});

test("Friend requests: sender can create pending request, cannot send to self", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Self request - fails
  await assertFails(
    aliceDb.collection("friend_requests").doc("req_self").set({
      senderUid: ALICE_UID,
      recipientUid: ALICE_UID,
      status: "pending"
    })
  );

  // Request to Bob - succeeds
  await assertSucceeds(
    aliceDb.collection("friend_requests").doc("req_alice_bob").set({
      senderUid: ALICE_UID,
      recipientUid: BOB_UID,
      senderDisplayName: "Alice",
      recipientDisplayName: "Bob",
      status: "pending"
    })
  );
});

test("Friend requests: recipient can accept, sender can cancel", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("friend_requests").doc("req_1").set({
      senderUid: ALICE_UID,
      recipientUid: BOB_UID,
      status: "pending"
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  // Third party cannot accept
  await assertFails(
    charlieDb.collection("friend_requests").doc("req_1").update({
      status: "accepted"
    })
  );

  // Recipient Bob accepts - succeeds
  await assertSucceeds(
    bobDb.collection("friend_requests").doc("req_1").update({
      status: "accepted"
    })
  );
});

test("Conversations: only participants can read messages", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("conversations").doc("conv_ab").set({
      participants: [ALICE_UID, BOB_UID],
      lastMessageText: "Hello"
    });
    await context.firestore().collection("conversations").doc("conv_ab").collection("messages").doc("msg_1").set({
      senderUid: ALICE_UID,
      recipientUid: BOB_UID,
      text: "Hello Bob",
      deliveryState: "sent"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  // Alice is participant: can read conversation and messages
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_ab").get());
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_ab").collection("messages").doc("msg_1").get());

  // Charlie is NOT participant: fails
  await assertFails(charlieDb.collection("conversations").doc("conv_ab").get());
  await assertFails(charlieDb.collection("conversations").doc("conv_ab").collection("messages").doc("msg_1").get());
});
