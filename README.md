# Automata-Animated-Visual-Toolkit

### Description
The **Automata-Animated-Visual-Toolkit** is a Java-based tool designed to help students and educators understand the fundamentals of automata theory. It provides an animated visual toolkit for simulating both deterministic and non-deterministic finite automata (DFA and NFA). The toolkit allows users to create, visualize, and simulate automata in a user-friendly environment.

---

## Table of Contents

- [Description](#description)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Technologies](#technologies)
- [Contact](#contact)

---

## Features

- Support for creating DFA and NFA
- Conversion from NFA to DFA using subset construction
- Visual simulation of automata with step-by-step execution
- Interactive input tape and real-time feedback
- Saving and loading automata states

---

## Installation

### Prerequisites
- **Java Development Kit (JDK)**: Version 21 or higher
- **JavaFX SDK**: Version 21.0.2 or higher (You can download it from [https://jdk.java.net/javafx21/](https://jdk.java.net/javafx21/))

### Steps
1. Clone the repository:
    ```bash
    git clone https://github.com/Shozab-N18/Automata-Animated-Visual-Toolkit
    ```
2. Navigate to the project directory:
    ```bash
    cd ./Automata-Animated-Visual-Toolkit
    ```
3. Run the jar file
    ```bash
    java --module-path ./lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.fxml -jar Automata-Animated-Visual-Toolkit.jar
    ```

---

## Usage

1. **Creating Automata**:
   - Use the interactive canvas to add states and transitions.
   - Define start and accept states for your automata.
   - Toggle between DFA and NFA modes.

2. **Simulating Automata**:
   - Input a word for the automaton to simulate.
   - Use the play, pause, and step functions to control the simulation.
   - Get feedback about the input acceptance.

3. **Converting NFA to DFA**:
   - Use the built-in subset construction algorithm to convert NFAs to DFAs.
   - Visualize and compare both automata on separate canvases.

4. **Error Handling**:
   - The app validates automata before simulation and provides feedback on whether the automaton is correctly defined.

---

## Technologies

- Java
- JavaFX
- JUnit
- Mockito

---

## Contact

For questions, feel free to reach out:

- **Name**: Shozab Anwar Siddique
- **Email**: [shozabamwarsiddique@gmail.com](mailto:shozabamwarsiddique@gmail.com)
- **LinkedIn**: [Your LinkedIn Profile](https://linkedin.com/in/shozabamwarsiddique)
- **GitHub**: [Your GitHub Profile](https://github.com/Shozab-N18)
