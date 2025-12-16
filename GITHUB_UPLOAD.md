# Commandes Git pour Upload sur GitHub

## Étape 1: Configuration Git (si pas déjà fait)
```bash
git config --global user.name "VotreNom"
git config --global user.email "votre.email@example.com"
```

## Étape 2: Ajouter les fichiers au staging
```bash
git add .
```

## Étape 3: Créer le premier commit
```bash
git commit -m "Initial commit: ModerationSMP Plugin - All French comments translated to English"
```

## Étape 4: Créer le dépôt sur GitHub
1. Allez sur https://github.com/new
2. Nommez votre dépôt: `ModerationSMP`
3. Choisissez Public ou Private
4. **NE PAS** ajouter de README, .gitignore ou license (on les a déjà)
5. Cliquez sur "Create repository"

## Étape 5: Connecter au dépôt GitHub
**Remplacez `YOUR_USERNAME` par votre nom d'utilisateur GitHub:**
```bash
git remote add origin https://github.com/YOUR_USERNAME/ModerationSMP.git
```

## Étape 6: Renommer la branche en main
```bash
git branch -M main
```

## Étape 7: Push vers GitHub
```bash
git push -u origin main
```

---

## Commandes Utiles pour Plus Tard

### Vérifier le statut
```bash
git status
```

### Ajouter des modifications futures
```bash
git add .
git commit -m "Description de vos changements"
git push
```

### Voir l'historique
```bash
git log --oneline
```

### Voir les fichiers ignorés
```bash
git status --ignored
```

---

## Notes Importantes

✅ **Fichiers créés:**
- `.gitignore` - Ignore les fichiers compilés et config
- `README.md` - Documentation du projet

⚠️ **Avant de push:**
- Vérifiez qu'il n'y a pas de données sensibles
- Les fichiers `target/`, `*.log` sont ignorés automatiquement
- Les configs serveur sont ignorés

🔒 **Si dépôt privé:**
- Seuls vous et les collaborateurs invités pourront voir le code

🌍 **Si dépôt public:**
- Tout le monde peut voir et cloner votre code
- Parfait pour un portfolio ou projet open-source
