# 🚖 **Taxi Meter **

---

## 📜 Description du projet

- **Taxi Meter** est une application Android en Kotlin permettant de simuler le fonctionnement d'un compteur de taxi. L'application doit calculer et afficher en temps réel la distance parcourue, le temps de trajet et le tarif de la course, en prenant en compte les paramètres de base tels que le tarif au kilomètre et le temps d'attente. L'application doit également permettre l'inscription, la connexion, et l'affichage des informations personnelles des utilisateurs (nom, e-mail, âge, type de permis), ainsi que la génération d'un QR code dynamique pour chaque profil. L'application utilise Firebase pour l'authentification des utilisateurs et le stockage des données.
---

## ⚙️ Fonctionnalités principales

### 1. 📝 **Écran d'inscription** (SignUp Activity)
- Interface utilisateur simplifiée pour l'inscription des nouveaux utilisateurs.
- Champs de saisie pour :
  - 👤 Nom complet
  - 📧 Adresse e-mail
  - 🎂 Âge
  - 🪪 Type de permis
  - 🔑 Mot de passe sécurisé
- Validation des champs pour assurer la précision des données.
- Enregistrement sécurisé via **Firebase Authentication**.
- Lien de redirection vers l'écran de connexion pour les utilisateurs existants.

### 2. 🔑 **Écran de connexion** (SignIn Activity)
- Authentification rapide et sécurisée via **Firebase Authentication**.
- Gestion des erreurs pour les cas tels que les e-mails non enregistrés ou les mots de passe incorrects.

### 3. 👤 **Écran de profil** (ProfileFragment)
- Génération dynamique d'un **QR code** contenant les informations utilisateur.
- Animations fluides pour une transition visuelle agréable.
- Bouton de déconnexion sécurisé avec retour à l'écran de connexion.
- Bouton "Retour" pour simplifier la navigation.

### 4. 🛠️ **Backend avec Firebase**
- **Firebase Authentication** 🔐 : Gestion sécurisée des utilisateurs (inscription, connexion, déconnexion).
- **Firebase Firestore** 🗄️ : Stockage sécurisé des données utilisateur et récupération en temps réel.

---

## 📥 Installation

### 1. 🧑‍💻 **Cloner le dépôt**
```bash
git clone https://github.com/username/taxixact.git
cd taxixact
```
### 2. 🔧 Configurer Firebase
1. Créez un projet sur [Firebase Console](https://console.firebase.google.com/).
2. Téléchargez le fichier `google-services.json` et placez-le dans le dossier `app` de votre projet.
3. Activez **Firebase Authentication** et **Firestore** dans la console Firebase.

### 3. 🚀 Lancer l'application
1. Ouvrez le projet dans **Android Studio**.
2. Synchronisez les dépendances **Gradle**.
3. Exécutez l'application sur un appareil Android ou un émulateur.

---

## 🗂️ Structure du projet

### **Activities**
- `SplashActivity` : Gère le premier screen qui s'affiche.
- `OnBoardingActivity` : Gère l'onboarding screen.
- `SignUpActivity` : Gère l'inscription des utilisateurs.
- `SignInActivity` : Gère la connexion des utilisateurs.
- `MainActivity`.

### **Fragments**
- `ProfileFragment` : Affiche les informations de l'utilisateur et le QR code.
- `MapFragment` : Afficher le map dans le main activity.

### **Utils**
- Classes utilitaires pour la gestion des QR codes et des animations.

### **Firebase**
- Intégration de **Firebase Authentication** et **Firestore** pour la gestion des utilisateurs.

---

## 💻 Technologies utilisées

- **Langage** : Kotlin
- **Backend** : Firebase Authentication & Firestore
- **UI Design** : Material Design, animations avec `ObjectAnimator`
- **QR Code** : Génération dynamique avec la bibliothèque `ZXing`

---

## 👥 Contributeurs
<p align="center">
  <img src="https://github.com/user-attachments/assets/9352fa66-a243-4662-8bab-ec475ab88fc2" alt="linkedn_profile" width="250" height="250" style="clip-path: circle();">
</p>

- **NaoufalSDI**  
  Développeur principal et concepteur de l'application.

---

---

## 📧 Contact

Pour toute question ou suggestion, contactez-nous à : **souadi.naoufl@gmail.com**
