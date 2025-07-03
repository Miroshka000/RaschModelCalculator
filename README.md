<div align="center">

# 📏 Rasch Model Calculator

**A powerful desktop application for calculating and analyzing the Rasch model, built with JavaFX.**

</div>

---

<p align="center">
  <a href="#-about-the-project">About</a> •
  <a href="#-key-features">Features</a> •
  <a href="#-gallery">Gallery</a> •
  <a href="#-technologies-used">Technologies</a> •
  <a href="#-how-to-run">Run</a> •
  <a href="#-author">Author</a>
</p>

---

<div align="center">
    <a href="README.md">Русский</a> | <a href="README.en.md">English</a>
</div>

## 📖 О проекте

**Rasch Model Calculator** — это десктопное приложение, разработанное для специалистов в области психометрики, образования и социальных наук. Оно предоставляет интуитивно понятный интерфейс для проведения расчетов по [модели Раша](https://ru.wikipedia.org/wiki/Модель_Раша), анализа данных и визуализации результатов.

Приложение позволяет:
- Загружать данные из файлов `.xlsx`, `.xls` и `.csv`.
- Автоматически рассчитывать **уровни подготовки** испытуемых и **трудности** заданий.
- Проводить детальный **анализ соответствия (fit analysis)** с использованием метрик **Infit/Outfit MNSQ** и **ZSTD**.
- Визуализировать результаты с помощью интерактивной **карты Райта**.

## ✨ Ключевые особенности

- **Простой импорт**: Легко загружайте данные в популярных форматах.
- **Мгновенные расчеты**: Быстрый и точный итеративный алгоритм для вычисления параметров модели.
- **Глубокий анализ**: Оценивайте качество ваших данных с помощью встроенных статистик соответствия (Infit/Outfit).
- **Наглядная визуализация**: Интерактивная карта Райта для удобного сравнения распределений уровней подготовки и трудностей.
- **Современный интерфейс**: Приятный и отзывчивый UI, созданный с помощью `JavaFX` и `AtlantaFX`.
- **Кроссплатформенность**: Работает на Windows, macOS и Linux.

## 🖼️ Галерея

| Главный экран | Карта Райта |
| :---: | :---: |
| ![Главный экран](https://i.imgur.com/your-main-screen-image.png) | ![Карта Райта](https://i.imgur.com/your-wright-map-image.png) |
| _Таблицы с результатами и статистиками._ | _Визуальное распределение испытуемых и заданий._ |

*(Примечание: Замените ссылки на скриншоты вашего приложения)*

## 🛠️ Технологии

| Категория | Технология |
| :--- | :--- |
| **Язык** | `Java 17+` |
| **Фреймворк UI** | `JavaFX` |
| **Стилизация** | `CSS`, `AtlantaFX` |
| **Сборка** | `Gradle` |
| **Работа с Excel**| `Apache POI` |
| **Модульность** | `JPMS (Java Platform Module System)`|

## 🚀 Как запустить

1.  **Клонируйте репозиторий:**
    ```bash
    git clone https://github.com/Miroshka000/RaschModelCalculator.git
    cd RaschModelCalculator
    ```
2.  **Запустите сборку через Gradle:**
    ```bash
    ./gradlew run
    ```
    *(Для Windows используйте `gradlew.bat run`)*

Приложение запустится, и вы сможете загрузить свой файл с данными для анализа.

## 👨‍💻 Автор

**Miroshka**

- GitHub: [@Miroshka000](https://github.com/Miroshka000)
- Telegram: [@miroshka000](https://t.me/miroshka000)

---
<div align="center">
Made with ❤️ and Java
</div> 