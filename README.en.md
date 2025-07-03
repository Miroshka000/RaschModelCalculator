<div align="center">

# 📏 Rasch Model Calculator

**A powerful desktop application for calculating and analyzing the Rasch model, built with JavaFX.**

</div>

---

<p align="center">
  <a href="#-about-the-project">About</a> •
  <a href="#-key-features">Features</a> •
  <a href="#-technologies-used">Technologies</a> •
  <a href="#-how-to-run">Run</a> •
  <a href="#-author">Author</a>
</p>

---

<div align="center">
    <a href="README.md">Русский</a> | <a href="README.en.md">English</a>
</div>

## 📖 About The Project

**Rasch Model Calculator** is a desktop application designed for psychometricians, educators, and social scientists. It provides an intuitive interface for performing calculations using the [Rasch model](https://en.wikipedia.org/wiki/Rasch_model), analyzing data, and visualizing results.

The application allows you to:
- Load data from `.xlsx`, `.xls`, and `.csv` files.
- Automatically calculate person **abilities** and item **difficulties**.
- Perform a detailed **fit analysis** using **Infit/Outfit MNSQ** and **ZSTD** metrics.
- Visualize results with an interactive **Wright Map**.

## ✨ Key Features

- **Easy Import**: Effortlessly load data in popular formats.
- **Instant Calculations**: A fast and accurate iterative algorithm for computing model parameters.
- **In-depth Analysis**: Evaluate the quality of your data with built-in fit statistics (Infit/Outfit).
- **Clear Visualization**: An interactive Wright Map for easy comparison of ability and difficulty distributions.
- **Modern Interface**: A pleasant and responsive UI built with `JavaFX` and `AtlantaFX`.

## 🛠️ Technologies Used

| Category | Technology |
| :--- | :--- |
| **Language** | `Java 17+` |
| **UI Framework** | `JavaFX` |
| **Styling** | `CSS`, `AtlantaFX` |
| **Build Tool** | `Gradle` |
| **Excel Handling**| `Apache POI` |
| **Modularity** | `JPMS (Java Platform Module System)`|

## 🚀 How To Run

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Miroshka000/RaschModelCalculator.git
    cd RaschModelCalculator
    ```
2.  **Run the build using Gradle:**
    ```bash
    ./gradlew run
    ```
    *(For Windows, use `gradlew.bat run`)*

The application will launch, and you can load your data file for analysis.

## 👨‍💻 Author

**Miroshka**

- GitHub: [@Miroshka000](https://github.com/Miroshka000)
- Telegram: [@miroshka000](https://t.me/miroshka000)

---
<div align="center">
Made with ❤️ and Java
</div> 