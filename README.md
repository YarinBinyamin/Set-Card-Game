# Set Card Game
 
# Java Concurrency - Set Card Game

## **Overview**
This project implements a **concurrent version** of the **Set card game** in **Java**, focusing on **multithreading, synchronization, and concurrency control**. The game features multiple **human and AI players** competing to find valid "sets" of three cards. Players interact with the game via a **graphical user interface (GUI)**, and the system ensures fair player turn handling using **Java Threads and Synchronization**.

The main goals of this project are:
- **Practice concurrent programming** in Java.
- **Use thread synchronization** effectively.
- **Manage shared resources** using correct locking mechanisms.
- **Implement unit testing** to ensure correctness.

## **Features**
- **Multithreading:** Each player runs on a separate thread.
- **Java Synchronization:** Ensures fair gameplay without race conditions.
- **Automated Dealer System:** The dealer assigns cards and validates sets.
- **Graphical User Interface (GUI):** Built-in UI to display the game board.
- **Human & AI Players:** Simulated keypresses for non-human players.
- **Penalty & Reward System:** Players receive points for correct sets and penalties for mistakes.
- **Maven Build Support:** Easily compile and run the project with Maven.

## **Installation & Compilation**
1. **Clone the repository**:
   ```sh
   git clone https://github.com/YarinBinyamin/JavaSetGame.git
   cd JavaSetGame
   ```
2. **Compile the project using Maven**:
   ```sh
   mvn compile
   ```
3. **Run the game**:
   ```sh
   mvn exec:java
   ```
4. **Clean the project (optional)**:
   ```sh
   mvn clean
   ```

## **Game Rules & Execution**
### **Game Objective**
- Players compete to find **valid sets of 3 cards**.
- A **set** must satisfy **one of these conditions** for all four features (color, shape, number, shading):
  1. **All the same** across all cards.
  2. **All different** across all cards.

### **How the Game Works**
- The **Dealer Thread** distributes cards to a **3x4 game table**.
- Players **place tokens** on cards to claim a set.
- The **Dealer checks the set**:
  - ✅ **If valid:** Player gets a **point**, and cards are replaced.
  - ❌ **If invalid:** Player gets a **penalty** (temporary freeze).
- The game continues until **no more valid sets remain**.

## **Keyboard Controls**
Each player has **12 unique keys** mapped to the **3x4 grid**. Example:
```
Player A:  Q W E R   U I O P
           A S D F   J K L ;
           Z X C V   M , . /
```
Pressing a key **places or removes a token** on the corresponding card.

## **Project Structure**
```
📂 JavaSetGame/
│── 📂 src/                  # Contains Java source files
│── 📂 src/main/java/        # Game logic implementation
│── 📂 src/test/java/        # Unit tests
│── 📂 resources/            # Game configuration files
│── pom.xml                  # Maven build configuration
│── README.md                # Documentation
```

## **Synchronization & Concurrency**
- **Players run as threads** and interact with the game concurrently.
- **Locks & Synchronization:**
  - **Players must notify the dealer** when selecting a set.
  - **The dealer processes sets in FIFO order** to ensure fairness.
  - **Players are frozen** when penalized or after scoring a point.
  - **Dealer periodically reshuffles** the deck if no valid sets exist.

## **Testing & Debugging**
- **Unit tests** are provided to verify key game components.
- **Run all tests** with Maven:
  ```sh
  mvn test
  ```
- **Memory & Performance Checks:**
  - Avoid excessive synchronization that may slow down the game.
  - Ensure players **do not lock shared resources unnecessarily**.

## **Bonus Challenges (Optional)**
Earn **bonus points** by implementing advanced features:
- **Support all configuration fields** dynamically.
- **Gracefully terminate all threads** in reverse order.
- **Enhance game efficiency** to avoid unnecessary thread wake-ups.

## **Contribution**
Feel free to contribute by:
- Reporting issues.
- Suggesting improvements.
- Submitting pull requests.

## **License**
This project is licensed under the **MIT License**.
