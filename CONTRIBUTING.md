# Contributing to Forgero

If you want to improve a feature or fix a bug in Forgero, feel free to fork the repo and create a pull request.  
You can also simply open an issue with the tag `enhancement`.

1. [Fork](https://github.com/sigmundgranaas/forgero/fork) Forgero to your Github account
2. Set up the project as described in the [development section](#development)
3. Create your feature branch: `git checkout -b feature/amazing-feature`
4. Commit your changes: `git commit -m 'Add an amazing feature'`
5. Push to the branch: `git push origin feature/amazing-feature`
6. Open a [pull request](https://github.com/sigmundgranaas/forgero/pulls)

Don't forget to give the project a star! Thanks again!  
Your contributions are **greatly appreciated**.

Thank you to Forgero's contributors (
see [`CONTRIBUTORS.md`](https://github.com/SigmundGranaas/forgero/blob/1.19/CONTRIBUTORS.md)).


<!-- DEVELOPMENT -->

## Development

Modding Minecraft is very easy thanks to the tools developed by the modding community.

### Prerequisites

#### IntelliJ IDEA

IntelliJ IDEA should already come with their own Java JDK and Gradle version out of the box, and is recommended for
Minecraft development:

- [IntelliJ IDEA](https://www.jetbrains.com/idea/download)
- [Minecraft Development plugin](https://mcdev.io/)

#### Different IDE/no IDE

If you don't want to use IntelliJ IDEA, but instead you want to use another IDE (or no IDE) that doesn't contain these
prerequisites out of the box:

* Your preferred build of Java 17, we recommend using [Adoptium Temurin OpenJDK 17](https://adoptium.net/temurin/)
* [Gradle](https://gradle.org/)

### Setting up the repository

1. Clone this repository (can be done via the command line or your IDE):
   ```sh
   git clone https://github.com/sigmundgranaas/forgero.git
   ```

2. Open Forgero in IntelliJ IDEA (or your IDE of choice/no IDE):
   ```sh
   new > project from existing sources > choose forgero folder
   ```
3. Make changes and run Forgero (can be done via the command line or your IDE):
   ```sh
   ./gradlew runClient
   ```
