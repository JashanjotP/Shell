# Shell Application

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Installation](#installation)
- [Usage](#usage)
- [Commands](#commands)
- [Contributing](#contributing)

## Technologies used

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)
![JLine](https://img.shields.io/badge/JLine-239120?style=for-the-badge&logo=jline&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

## Introduction

Welcome to the Shell Application! This is a custom shell implemented in Java, providing a command-line interface to execute both built-in and external commands. The shell supports tab completion, command history, and piping of commands.

## Features

- **Built-in Commands**: Supports basic shell commands like `cd`, `echo`, `exit`, `type`, `pwd`, `ls`, and `history`.
- **External Commands**: Execute external commands available in the system's PATH.
- **Tab Completion**: Autocomplete for both built-in and external commands.
- **Command Piping**: Supports piping output of one command to another.
- **Command History**: Navigate through command history using the up and down arrow keys.

## Technology Stack

- **Java**: The primary language used for implementing the shell.
- **JLine**: Library used for handling terminal input and providing features like tab completion and command history.

## Installation

To run this application locally, follow these steps:

1. **Clone the repository**:

    ```bash
    git clone https://github.com/JashanjotP/Shell.git
    cd Shell/Shell
    ```

2. **Build the project** using Maven:

    ```bash
    mvn clean install
    ```
## Usage

1. **Run the shell application**:

    ```bash
    java -jar target/Shell-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

2. **Execute commands**: You can execute both built-in and external commands.
3. **Use tab completion**: Start typing a command and press the Tab key to autocomplete.
4. **Navigate command history**: Use the up and down arrow keys to navigate through previously entered commands.
5. **Use piping**: Chain commands using the pipe (`|`) symbol.

## Commands

### Built-in Commands

- `cd [directory]`: Change the current directory.
- `echo [text]`: Print text to the terminal.
- `exit`: Exit the shell.
- `type [command]`: Display the type of command (builtin or external).
- `pwd`: Print the current working directory.
- `ls`: List files and directories in the current directory.
- `history`: Display the history of entered commands.

### External Commands

- Any executable available in the system's PATH can be executed.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any improvements or bug fixes. For major changes, please open an issue first to discuss what you would like to change.
