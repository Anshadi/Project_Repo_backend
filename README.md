# Voice Shopping Assistant

A complete voice-enabled shopping web application with Flutter frontend and Spring Boot backend, featuring AI-powered recommendations and intelligent voice command processing.

## Features Implemented

1. **App Shell** + Bottom navigation with 4 tabs
2. **Shopping List Screen** (Main Page) - Manage shopping lists with voice commands
3. **Search & Item Lookup Screen** - Search for products
4. **History Screen** - View purchase history and analytics
5. **Settings Screen** - App preferences and theme management
6. **Shared Lists Screen** - Share shopping lists with others
7. **User Profile Screen** - User account management
8. **UI Components** - Voice input widget, item cards, product cards, suggestions

## Tech Stack Used

- **Flutter**: Cross-platform web framework
- **Dart**: Programming language
- **speech_to_text**: Voice recognition (local device)
- **http**: REST API communication
- **permission_handler**: Microphone permissions
- **shared_preferences**: Local storage for theme preference

## Deployment

- **Frontend**: Hosted on [Render](https://project-repo-0.onrender.com/)
- **Backend**: Hosted on [Render](https://projectrepobackend-production.up.railway.app)

## Project Structure

```
lib/
├── main.dart                    # App entry point with dark mode support
├── models/                      # Data models
├── services/
│   ├── api_service.dart         # Backend API communication
│   ├── speech_service.dart      # Speech-to-text functionality
│   ├── theme_service.dart       # Theme preference management
│   └── user_service.dart        # User data management
├── screens/
│   ├── splash_screen.dart       # Initial loading screen
│   ├── main_navigation.dart     # Bottom navigation container
│   ├── shopping_list_screen.dart # Main shopping list view
│   ├── search_screen.dart       # Product search view
│   ├── history_screen.dart      # Purchase history and analytics
│   ├── settings_screen.dart     # App settings and preferences
│   ├── shared_lists_screen.dart # Shared shopping lists
│   └── user_profile_screen.dart # User profile management
├── utils/                       # Utility functions
├── widgets/                     # Reusable UI components
├── analysis_options.yaml
├── pubspec.yaml
└── pubspec.lock
web/                            # Web-specific files
build/                          # Build output
```

## Prerequisites

- **Flutter SDK 3.x** - Install from [flutter.dev](https://flutter.dev/docs/get-started/install)
- **Git** - For cloning the repository

## Setup Instructions

### Step 1: Clone the Repository
```bash
git clone https://github.com/Anshadi/Project_Repo.git
cd Project_Repo
```

### Step 2: Install Flutter Dependencies
```bash
flutter pub get
```

### Step 3: Run the Application
```bash
flutter run -d chrome
```

For web development:
```bash
flutter run -d web-server --web-port 8080
```

### Step 4: Configuration

1. **Update API endpoint** (if needed)
   Edit `lib/services/api_service.dart`:
   ```dart
   static const String baseUrl = 'https://your-backend-url.onrender.com/api';
   ```

2. **Enable microphone permissions**
   - Web: Browser will prompt for microphone access
   - Permissions are handled automatically by Flutter web

## Permissions Required

### Web Browser:
- **Microphone Access**: For voice recognition
- **Network Access**: For API communication

## Recently Added Features

### Dark Mode Theme
- **Complete dark theme implementation** with automatic theme switching
- **Persistent theme preference** using SharedPreferences
- **Adaptive colors** that work well in both light and dark modes
- **Theme toggle** available in Settings screen
- **System theme detection** capability

### Shopping History Visualization
- **Complete purchase history tracking** with detailed analytics
- **Interactive charts and statistics**:
  - Total items purchased and quantities
  - Total amount spent and average per item
  - Category breakdown with visual charts
  - Monthly purchase trends
- **Filtering options** (This Week, This Month, Last 3 Months)
- **Detailed item history** with store information and prices
- **Export functionality** for data backup
- **Two-tab interface**: History timeline and Analytics dashboard

### Enhanced Navigation
- **4-tab bottom navigation**: Shopping List, Search, History, Settings
- **Settings screen** with comprehensive preferences
- **Theme management** and app customization options

### Additional Features
- **Shared Lists**: Share shopping lists with other users
- **User Profile**: Manage user account and preferences
- **Voice Commands**: Advanced voice recognition for adding items, searching, etc.

## Required Backend Endpoints

The frontend expects the following REST API endpoints to be implemented in your Spring Boot backend:

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

## About

This is a repository for pushing ready projects
