JAVA FILM MASTER 

## FilmStore Project README

### Overview

FilmStore is a Java-based application designed to manage a film store's inventory, user accounts, shopping carts, and transactions. This project incorporates multiple components such as entity classes, managers, services, and CSV-based data storage.

### Features

1. **User Management**: Handle user registration, authentication, and account management.
2. **Film Management**: Manage film inventory including adding, updating, and deleting films.
3. **Cart Management**: Allow users to add films to their shopping carts and manage cart contents.
4. **Purchase History**: Track user purchase history and generate invoices.
5. **Comments**: Enable and manage user comments on films.

### Project Structure

#### Directories

- **CSVBase**: Contains CSV files used for data storage.
- **Entities**: Contains Java classes representing the main entities (User, Admin, Film, Comment).
- **Manager**: Contains Java classes that manage various aspects of the application (CartManager, CSVManager, FilmManager, UserManager).
- **Services**: Contains Java classes for the main application services and UI components (LoginWindow, MainApplication, FilmDisplay).
- **Invoices**: Contains generated invoice files.

#### Key Files

- `FilmStore.iml`: IntelliJ IDEA project file.
- `README.md`: Project documentation.
- `umlduprojet.puml` & `umlduprojet.png`: UML diagram of the project.
- `.gitignore`: Git ignore file.
- `src/`: Source code directory.

### Installation

1. **Clone the repository**:
    ```bash
    git clone <repository-url>
    ```
2. **Open the project in IntelliJ IDEA**:
    - Navigate to `File -> Open` and select the project directory.
3. **Build the project**:
    - Ensure all dependencies are installed and build the project using IntelliJ IDEA's build tools.

### Usage

1. **Run the application**:
    - Execute the `MainApplication.java` file from the `src/Services` directory.
2. **Login or Register**:
    - Use the login window to authenticate or create a new account.
3. **Browse Films**:
    - View the list of available films, add films to the cart, and manage the cart.
4. **Manage Account**:
    - Update account details, view purchase history, and manage subscriptions.

### Data Management

- **CSV Files**: Data is stored in CSV files located in the `CSVBase` directory. These include files for users, films, carts, purchase history, and comments.

### Development

To contribute to this project:

1. **Fork the repository**.
2. **Create a new branch** for your feature or bugfix.
3. **Make your changes** and commit them.
4. **Push to your branch** and create a pull request.

### License

This project is licensed under the CY TECH License.

### Contact

For any inquiries or support, please contact the project maintainer at [khaldimohamedamine78@gmail.com].
