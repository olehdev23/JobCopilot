# Job Copilot

**Your personal AI assistant for conquering job interviews.**

"Job Copilot" is an AI-powered Telegram bot designed to streamline the job application process. By leveraging the power of local Large Language Models (LLMs) via Ollama, it helps users prepare for interviews by providing tailored feedback on their resume and a target job description.

---

## Key Features

* **AI-Powered Analysis:** Uses local LLMs (Llama 3) to analyze a resume and job vacancy.
* **Automated Cover Letter Generation:** Generates a personalized and professional cover letter highlighting relevant skills and experience.
* **Personalized Interview Tips:** Provides potential technical, behavioral, and research-based questions specific to the job vacancy.
* **Decoupled Architecture:** Built on a scalable microservices architecture to ensure high performance and maintainability.

## Architecture Overview

The application is built using a microservices architecture to ensure high availability, scalability, and maintainability. 

* **`bot-gateway`**: The API Gateway and entry point for all Telegram bot interactions. It orchestrates communication with other microservices.
* **`user-data-access`**: A dedicated microservice for managing and persisting user data. It has its own isolated database.
* **`analysis-service`**: A specialized microservice for AI-powered analysis. It processes data asynchronously via Kafka and interacts with a locally running Ollama instance.
* **`common-dto`**: A shared library containing common Data Transfer Objects (DTOs) and models used by all microservices, ensuring a consistent data format.
* **Centralized Configuration**: All modules are part of a multi-module Maven project with a parent POM for centralized dependency management and code style enforcement via Checkstyle.

---

## Getting Started

### Prerequisites

* **Java 21** or higher
* **Maven**
* **Docker** and **Docker Compose**
* **Ollama** (running locally)

### Local Development

1.  **Clone the repository:**
    ```bash
    git clone [your-repository-url]
    cd telegram-bot
    ```

2.  **Start Ollama:**
    Ensure Ollama is running in your terminal. If you don't have it, install it and run `ollama pull llama3` to download the model.
    ```bash
    ollama serve
    ```

3.  **Create `.env` file:**
    Create a file named `.env` in the root directory of the project. It should contain the following environment variables. **Do not use the provided values, they are examples!**
    
    ```
    # Bot credentials
    TELEGRAM_BOT_TOKEN=YOUR_TELEGRAM_BOT_TOKEN

    # Database credentials
    POSTGRES_USER=YOUR_POSTGRES_USER
    POSTGRES_PASSWORD=YOUR_POSTGRES_PASSWORD
    POSTGRES_DB=YOUR_POSTGRES_DB

    # Microservices URLs
    USER_DATA_ACCESS_URL=http://user-data-access:8081
    OLLAMA_URL=[http://host.docker.internal:11434](http://host.docker.internal:11434)

    # Spring profile
    SPRING_PROFILE=dev
    ```

4.  **Build and run the application:**
    Use Docker Compose to build and start all microservices. The `docker-compose.yml` file is configured to connect to your local Ollama instance.
    ```bash
    docker-compose up --build
    ```

    _Note: If you encounter build issues, try running `mvn clean package` first to build the project locally before using Docker Compose._

---
## Contributing

Contributions are welcome! If you find any issues or have suggestions for improvements, please open an issue or submit a pull request.

---

