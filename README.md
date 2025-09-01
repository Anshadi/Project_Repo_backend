# Voice Shopping Backend

Spring Boot backend for Voice Shopping Assistant, providing AI-powered recommendations, voice processing, and shopping list management.

## Tech Stack Used

- **Spring Boot 3.2.0**: Framework for building REST APIs
- **Java 17**: Programming language
- **MongoDB**: NoSQL database for data storage
- **Google Gemini AI**: For AI-powered recommendations and voice processing
- **Maven**: Build tool
- **Docker**: Containerization
- **Railway**: Deployment platform

## Features

- **Voice/NLP Processing**: Process voice commands using Google Gemini AI
- **Shopping List Management**: CRUD operations for shopping lists and items
- **Recommendations**: AI-powered product recommendations
- **Product Search**: Search for products
- **User History**: Track purchase history
- **User Preferences**: Manage user preferences

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── voice/
│   │           └── shopping/
│   │               ├── VoiceShoppingApplication.java
│   │               ├── config/
│   │               ├── controller/
│   │               ├── dto/
│   │               ├── model/
│   │               ├── repository/
│   │               └── service/
│   └── resources/
│       ├── application.yml
│       └── .env
├── test/
target/
pom.xml
Dockerfile
docker-compose.yml
render.yaml
```

## Prerequisites

- **Java 17+** - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
- **Maven 3.6+** - Download from [maven.apache.org](https://maven.apache.org/download.cgi)
- **Git** - For cloning the repository
- **MongoDB Atlas account** - Create free account at [mongodb.com](https://www.mongodb.com/atlas)
- **Google Gemini API key** - Get from [Google AI Studio](https://makersuite.google.com/app/apikey)

## Setup Instructions

### Step 1: Clone the Repository

```bash
git clone https://github.com/Anshadi/Project_Repo_backend.git
cd Project_Repo_backend
```

### Step 2: Create Environment Configuration

Create a `.env` file in the root directory:

```env
GEMINI_API_KEY=your-gemini-api-key-here
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/voice_shopping
SERVER_PORT=8082
GEMINI_API_MODEL=gemini-1.5-flash
GEMINI_API_MAX_TOKENS=150
```

### Step 3: Install Dependencies and Run

```bash
mvn clean install
mvn spring-boot:run
```

Or use Docker:

```bash
docker-compose up
```

### Step 4: Verify Backend is Running

- Backend will start on `http://localhost:8082`
- Check health endpoint: `http://localhost:8082/api/health`

## API Endpoints

- `GET /api/health` - Health check
- `GET /api/products/search?q={query}` - Search products
- `POST /api/lists` - Create shopping list
- `GET /api/lists` - Get user's shopping lists
- `PUT /api/lists/{id}` - Update shopping list
- `DELETE /api/lists/{id}` - Delete shopping list
- `POST /api/lists/{id}/items` - Add item to list
- `PUT /api/lists/{id}/items/{itemId}` - Update item
- `DELETE /api/lists/{id}/items/{itemId}` - Delete item
- `GET /api/history` - Get purchase history
- `POST /api/history` - Add purchase to history
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `POST /api/voice/process` - Process voice commands
- `GET /api/recommendations` - Get product recommendations

## Deployment

- **Backend**: Hosted on [Railway](https://projectrepobackend-production.up.railway.app)

## Configuration

- **Database**: MongoDB Atlas
- **AI Service**: Google Gemini API
- **Port**: 8082
- **CORS**: Configured for frontend access
