# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Memotter is a Twitter-like timeline memo application for Android that focuses on efficient recording and management of daily notes. The project emphasizes offline functionality and uses markdown format for memo storage.

## Key Architecture Concepts

### Core Features (Must Have)
- Memo creation, display, editing, and deletion with timeline view
- Hashtag functionality with history and auto-completion
- Search capabilities (hashtag and text-based)
- Markdown syntax support
- Voice input with speech recognition
- Automatic local backup
- Full offline support

### Data Management
- **File Storage Options**: Two modes configurable by user
  - Daily file mode: Auto-creates daily markdown files
  - Continuous file mode: Continues from last opened file
- **Backup System**: Multi-generation local backups with 7-day retention for deleted memos
- **Template System**: Reads markdown templates from configurable template directory

### Technical Constraints
- Target: Android 7.0 (API level 24) and above
- Offline-first design with minimal network dependencies
- Local storage-centric architecture
- Permissions required: voice recording, storage access

## Development Approach

### Priority Implementation
1. **MVP Phase**: Core memo functionality (create, display, edit, delete)
2. **Enhancement Phase**: Hashtag system, search, voice input
3. **Polish Phase**: UI improvements, templates, favorites
4. **Future Phase**: Cloud sync (Google Drive integration)

### Key Design Principles
- Simplicity over complexity
- Fast startup time (under 3 seconds)
- Responsive UI with dark mode support
- Efficient storage usage
- Maintainable architecture using Android standard technologies

## File Structure Notes

- `dev_memo.md`: Complete requirements specification in Japanese
- Templates stored in user-configurable directory
- Deleted memos temporarily stored in "削除した投稿" file for 7-day recovery period