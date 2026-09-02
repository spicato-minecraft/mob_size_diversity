# Release changelog template

Copy this file to `releases/{minecraft_version}-{mod_version}.md` before creating a GitHub Release.

Write for players, not developers. Focus on what changed in the game.

---

## Example

```markdown
- Mobs now spawn with a range of sizes instead of one silhouette per type
- Elite mobs use a larger size band than commons
```

## Guidelines

- Use plain language ("zombies" not "Attributes.SCALE")
- One bullet per player-visible change
- Omit internal refactors, test additions, and CI changes unless players notice them
- Do not copy PR titles or commit messages
