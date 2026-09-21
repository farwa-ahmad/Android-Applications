const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const { after, before, beforeEach } = require("node:test");
const assert = require("node:assert/strict");

const {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} = require("@firebase/rules-unit-testing");
const {
  Timestamp,
  deleteDoc,
  doc,
  getDoc,
  setDoc,
  updateDoc,
} = require("firebase/firestore");

let testEnv;

function task(overrides = {}) {
  return {
    task: "Buy milk",
    due: "2026-09-22",
    dueTime: "09:30",
    status: 0,
    time: Timestamp.fromMillis(1_700_000_000_000),
    ...overrides,
  };
}

function taskRef(context, userId = "alice", taskId = "task-1") {
  return doc(context.firestore(), "users", userId, "tasks", taskId);
}

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "mylist-rules-test",
    firestore: {
      rules: fs.readFileSync(
        path.join(__dirname, "firestore.rules"),
        "utf8"
      ),
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test("owner can create, read, update, and delete a valid task", async () => {
  const alice = testEnv.authenticatedContext("alice");
  const ref = taskRef(alice);

  await assertSucceeds(setDoc(ref, task()));
  await assertSucceeds(getDoc(ref));
  await assertSucceeds(updateDoc(ref, { task: "Buy oat milk", status: 1 }));
  await assertSucceeds(deleteDoc(ref));
});

test("unauthenticated users cannot read or write tasks", async () => {
  const guest = testEnv.unauthenticatedContext();

  await assertFails(getDoc(taskRef(guest)));
  await assertFails(setDoc(taskRef(guest), task()));
});

test("authenticated users cannot access another user's tasks", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await setDoc(taskRef(context, "alice"), task());
  });

  const bob = testEnv.authenticatedContext("bob");
  await assertFails(getDoc(taskRef(bob, "alice")));
  await assertFails(setDoc(taskRef(bob, "alice", "task-2"), task()));
  await assertFails(deleteDoc(taskRef(bob, "alice")));
});

test("task text must be non-empty and at most 500 characters", async () => {
  const alice = testEnv.authenticatedContext("alice");

  await assertFails(setDoc(taskRef(alice, "alice", "empty"), task({ task: "" })));
  await assertFails(
    setDoc(
      taskRef(alice, "alice", "too-long"),
      task({ task: "x".repeat(501) })
    )
  );
});

test("unknown task fields are rejected", async () => {
  const alice = testEnv.authenticatedContext("alice");

  await assertFails(
    setDoc(
      taskRef(alice),
      task({ admin: true })
    )
  );
});

test("due date must match YYYY-MM-DD when present", async () => {
  const alice = testEnv.authenticatedContext("alice");

  await assertFails(
    setDoc(taskRef(alice, "alice", "legacy-date"), task({ due: "22/9/2026" }))
  );
  await assertFails(
    setDoc(taskRef(alice, "alice", "bad-month"), task({ due: "2026-13-22" }))
  );
});

test("due time requires a due date and valid 24-hour HH:mm format", async () => {
  const alice = testEnv.authenticatedContext("alice");

  await assertFails(
    setDoc(taskRef(alice, "alice", "time-without-date"), task({ due: "", dueTime: "09:30" }))
  );
  await assertFails(
    setDoc(taskRef(alice, "alice", "bad-time"), task({ dueTime: "25:10" }))
  );
});

test("due and dueTime may be omitted", async () => {
  const alice = testEnv.authenticatedContext("alice");
  const data = task();
  delete data.due;
  delete data.dueTime;

  await assertSucceeds(setDoc(taskRef(alice), data));
});

test("status must be 0 or 1", async () => {
  const alice = testEnv.authenticatedContext("alice");

  await assertFails(setDoc(taskRef(alice), task({ status: 2 })));
  await assertFails(setDoc(taskRef(alice, "alice", "string-status"), task({ status: "0" })));
});

test("creation timestamp cannot be changed after task creation", async () => {
  const alice = testEnv.authenticatedContext("alice");
  const ref = taskRef(alice);

  await assertSucceeds(setDoc(ref, task()));
  await assertFails(
    updateDoc(ref, { time: Timestamp.fromMillis(1_800_000_000_000) })
  );

  const snapshot = await getDoc(ref);
  assert.equal(snapshot.data().time.toMillis(), 1_700_000_000_000);
});
