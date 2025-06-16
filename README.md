# Memotter

Twitterライクなタイムライン形式の日常メモアプリです。

## 特徴

- 📝 **簡単メモ作成**: Twitter風のシンプルなインターフェース
- 🏷️ **ハッシュタグ機能**: 日本語ハッシュタグ完全対応
- 🎤 **音声入力**: Android標準音声認識対応
- 🔍 **強力な検索**: 全文検索・ハッシュタグ検索
- ❤️ **お気に入り**: 重要なメモをお気に入り登録
- 🌙 **ダークモード**: 目に優しいダークテーマ
- 📱 **オフライン対応**: ネットワークなしでも利用可能

## 技術仕様

- **言語**: Kotlin
- **最低SDK**: Android 7.0 (API 24)
- **アーキテクチャ**: MVP パターン
- **データベース**: Room (SQLite)
- **UI**: Material Design 3

## 主な機能

### Must Have（実装済み）
- ✅ メモ作成・表示・編集・削除
- ✅ ハッシュタグ機能
- ✅ 検索機能（ハッシュタグ・単語）
- ✅ タイムライン表示
- ✅ マークダウン記法対応
- ✅ 音声入力
- ✅ 自動バックアップ
- ✅ オフライン対応

### Should Have（実装済み）
- ✅ お気に入り機能
- ✅ テンプレート機能
- ✅ 削除メモ復元機能
- ✅ ダークモード
- ✅ フォントサイズ調整

## プロジェクト構造

```
app/src/main/java/com/example/memotter/
├── data/
│   ├── database/           # Room DAO, Database
│   ├── repository/         # Repository pattern
│   └── model/             # Data models
├── ui/
│   ├── timeline/          # タイムライン表示
│   ├── memo/              # メモ作成・編集
│   ├── search/            # 検索機能
│   ├── favorites/         # お気に入り
│   ├── templates/         # テンプレート
│   └── settings/          # 設定
└── util/                  # ユーティリティ
```

## 開発について

詳細な開発過程は `doc/` ディレクトリを参照してください：

- `doc/development_plan.md` - 開発計画書
- `doc/progress_log.md` - 開発進捗ログ
- `doc/project_summary.md` - プロジェクト総括

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。