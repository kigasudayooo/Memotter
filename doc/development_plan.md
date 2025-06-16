# Memotter開発計画書

## 開発フェーズ

### Phase 1: プロジェクト基盤構築
- [x] 要件定義書作成 (dev_memo.md)
- [x] CLAUDE.md作成
- [ ] Androidプロジェクト初期化
- [ ] プロジェクト構造とビルド環境設定
- [ ] 基本的なUI/UXフレームワーク選定

### Phase 2: MVP実装 (Must Have機能)
- [ ] データモデル設計・実装
- [ ] メモ作成機能
- [ ] メモ表示機能（タイムライン）
- [ ] ローカルストレージ実装
- [ ] 基本的な検索機能

### Phase 3: 基本機能強化
- [ ] ハッシュタグ機能
- [ ] マークダウン記法対応
- [ ] メモ編集・削除機能
- [ ] 音声入力機能

### Phase 4: 高度な機能
- [ ] 自動バックアップ
- [ ] お気に入り機能
- [ ] テンプレート機能
- [ ] ダークモード対応

## 技術選択

### 開発環境
- Android Studio
- Kotlin
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: Latest stable

### アーキテクチャ
- MVP (Model-View-Presenter) パターン
- Repository パターンによるデータ層抽象化
- Room データベース (SQLite)
- ViewModel + LiveData

### 主要ライブラリ候補
- Room: ローカルデータベース
- RecyclerView: タイムライン表示
- Material Design Components: UI
- Speech Recognition API: 音声入力
- Markwon: マークダウン表示

## ファイル構成予定

```
app/
├── src/main/
│   ├── java/com/example/memotter/
│   │   ├── data/
│   │   │   ├── database/
│   │   │   ├── repository/
│   │   │   └── model/
│   │   ├── ui/
│   │   │   ├── memo/
│   │   │   ├── timeline/
│   │   │   └── common/
│   │   ├── util/
│   │   └── MainActivity.kt
│   ├── res/
│   └── AndroidManifest.xml
├── build.gradle
└── proguard-rules.pro
```

## 開発メモ

### 2025/06/16
- プロジェクト初期化開始
- 要件定義書確認完了
- 開発計画書作成完了