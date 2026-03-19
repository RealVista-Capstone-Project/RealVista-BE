---
description: Interactive commit workflow enforcing project standards (Conventional Commits, Atomic Commits)
---

# Commit Workflow

This workflow guides you through creating commits that adhere to the project's [Commit Rules](file:///.agent/rules/commit-rules.md).

## 1. Check Status

First, let's see what has changed.

// turbo

1. Run `git status` to see modified files.

## 2. Select Files for Atomic Commit

**Rule:** Do NOT commit all changes at once unless they are strictly related.
**Goal:** Create an "atomic" commit (one logical change).

1. Decide which files belong to a _single_ logical change (e.g., just the widget, or just the translations).
2. Run `git add <file_path>` for ONLY those files.
   - _Tip: You can use `git add -p` for partial file commits if needed._

## 3. Verify Staged Changes

Make sure you are committing exactly what you intend.

// turbo

1. Run `git diff --cached --name-only` to confirm scoped files.
2. Run `git diff --cached` to review the actual code changes.

## 4. Draft Commit Message

Construct a message following **Conventional Commits**: `<type>(<scope>): <subject>`

### Choose Type:

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting (no code change)
- `refactor`: Code restructuring
- `test`: Adding tests
- `chore`: Maintenance

### Choose Scope:

- `widget`, `feature`, `screen`, `i18n`, `shared`, or specific component name.

### Write Subject:

- Imperative mood ("add" not "added")
- Lowercase first letter
- No period at end
- Max 50 chars

**Example:** `feat(widget): add property map section`

1. Construct your commit message.
2. Run `git commit -m "your_message"`
   - _If you need a body, use `git commit` to open the editor or `git commit -m "subject" -m "body"`_

## 5. Verification

1. Run `git log -1` to verify the commit looks correct.
2. Run `git status` to see if there are remaining files.

## 6. Repeat?

If `git status` shows more changes:

- Loop back to **Step 2** and create another commit for the remaining files.
- Continue until `git status` is clean.
