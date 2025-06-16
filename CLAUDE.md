# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Memotter is a Twitter-like timeline memo application for Android that focuses on efficient recording and management of daily notes. The project emphasizes offline functionality and uses markdown format for memo storage.

## Key Architecture Concepts

### Core Features (Implemented ✅)
- ✅ Memo creation, display, editing, and deletion with timeline view
- ✅ Hashtag functionality with history and auto-completion  
- ✅ Search capabilities (hashtag and text-based)
- ✅ Markdown syntax support with real-time rendering
- ✅ Voice input with speech recognition
- ✅ File management and editing capabilities
- ✅ Export functionality (directory and ZIP formats)
- ✅ Custom directory selection with persistent settings
- ✅ Full offline support

### Data Management
- **File Storage Options**: Two modes configurable by user
  - ✅ Daily file mode: Auto-creates daily markdown files
  - ✅ Continuous file mode: Continues from last opened file
  - ✅ Current file override: Opened files become active timeline
- **Directory Management**: Custom directory support
  - ✅ Default directory: `/Documents/Memotter/`
  - ✅ Custom directory selection via DirectorySelectDialog
  - ✅ Persistent settings using SharedPreferences
- **Export System**: 
  - ✅ Directory export with timestamped folders
  - ✅ ZIP compression for easy sharing
  - ✅ Includes all memos, templates, and deleted files
- **Template System**: ✅ Reads markdown templates from configurable template directory

### Technical Constraints
- Target: Android 7.0 (API level 24) and above
- Offline-first design with minimal network dependencies
- Local storage-centric architecture
- Permissions required: voice recording, storage access

## Development Approach

### Implementation Status
1. ✅ **MVP Phase**: Core memo functionality (create, display, edit, delete)
2. ✅ **Enhancement Phase**: Hashtag system, search, voice input
3. ✅ **Polish Phase**: UI improvements, file management, export functionality
4. 🔄 **Current Phase**: Feature completion and optimization
5. 🚀 **Future Phase**: Cloud sync (Google Drive integration)

### Key Design Principles
- Simplicity over complexity
- Fast startup time (under 3 seconds)
- Responsive UI with dark mode support
- Efficient storage usage
- Maintainable architecture using Android standard technologies

## Implementation Details

### Key Components
- **PreferencesManager**: Settings management with SharedPreferences
- **FileManager**: Local file operations with custom directory support
- **BackupManager**: Export functionality for directories and ZIP files
- **CurrentFileManager**: Active file state management
- **MarkdownFileParser**: Conversion between file content and memo objects
- **DirectorySelectDialog**: Directory browsing and selection UI

### File Structure
- `dev_memo.md`: Complete requirements specification in Japanese
- Templates stored in user-configurable directory
- Deleted memos temporarily stored in "削除した投稿" file for 7-day recovery period
- Custom directory support for flexible storage location

### Architecture Patterns
- **Repository Pattern**: MemoRepository for data management
- **MVVM**: ViewModels for UI state management
- **Navigation Component**: Fragment navigation with Safe Args
- **Room Database**: SQLite abstraction with LiveData
- **Markwon Library**: Markdown rendering with real-time preview

### User Interface
- **Material Design 3**: Modern Android UI components
- **NavigationDrawer**: Main navigation with categorized sections
- **Toolbar Menus**: Context-aware actions (file open, export, search)
- **Settings Screen**: Comprehensive app configuration
- **Dialog Fragments**: File operations and directory selection

### Export Functionality
- **Directory Export**: Creates timestamped folders with all files
- **ZIP Export**: Compressed archive for easy sharing
- **Access Points**: Settings screen and main menu
- **File Inclusion**: Memos, templates, and deleted files

## Recent Development History

### Major Features Implemented
1. **Continuous Tweet Functionality**: Files continue saving to same file until manually changed
2. **Markdown Display**: Real-time markdown rendering using Markwon library
3. **File Management System**: Open, edit, and manage markdown files directly
4. **Custom Directory Support**: User-configurable storage locations with persistence
5. **Export System**: Comprehensive backup and sharing capabilities

### Code Quality Improvements
- Removed unused kebab menu (three-dot menu) from memo items
- Cleaned up TODO implementations that were not functional
- Updated help documentation to reflect current features
- Streamlined MemoAdapter interface by removing unused callbacks

### Settings and User Experience
- Enhanced settings screen with data management section
- Added directory selection with visual path display
- Implemented export options with progress feedback
- Updated help content with current feature explanations

## Development Notes for Claude Code

### Build Commands
- **Debug Build**: `./gradlew assembleDebug`
- **Clean Build**: `./gradlew clean assembleDebug`

### Testing Approach
- Manual testing via Android emulator/device
- Focus on file operations and data persistence
- Verify export functionality across different directories

### Common Tasks
- Use `PreferencesManager` for all settings persistence
- Implement new dialogs as `DialogFragment` subclasses
- Follow existing file operation patterns in `FileManager`
- Maintain consistent UI patterns using Material Design 3