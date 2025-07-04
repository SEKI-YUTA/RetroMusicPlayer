# RetroMusicPlayer - プロジェクトアーキテクチャドキュメント

## 概要
RetroMusicPlayerは、Kotlin言語で開発されたAndroid向けの音楽プレイヤーアプリケーションです。モダンなAndroid開発のベストプラクティスに従い、保守性とスケーラビリティを重視した設計となっています。

## プロジェクト構造

### ルートディレクトリ構造
```
RetroMusicPlayer/
├── app/                    # メインアプリケーションモジュール
├── appthemehelper/        # テーマヘルパーライブラリ
├── assets/                # アセットファイル
├── fastlane/              # CI/CD設定
├── gradle/                # Gradleビルド設定
├── screenshots/           # スクリーンショット
└── gradle.properties      # Gradleプロパティ
```

### アプリケーションモジュール構造
```
app/src/main/
├── AndroidManifest.xml
├── assets/                # アプリケーションアセット
├── java/code/name/monkey/retromusic/
│   ├── App.kt            # アプリケーションクラス
│   ├── MainActivity.kt   # メインアクティビティ
│   ├── activities/       # アクティビティ群
│   ├── adapter/          # RecyclerViewアダプター
│   ├── db/              # データベース関連
│   ├── fragments/        # フラグメント群
│   ├── repository/       # データアクセスレイヤー
│   ├── service/          # バックグラウンドサービス
│   └── util/            # ユーティリティクラス
└── res/                  # リソースファイル
```

## アーキテクチャパターン

### 1. アーキテクチャの概要
RetroMusicPlayerは**レイヤード・アーキテクチャ**を採用し、以下の要素を組み合わせています：

- **MVVM (Model-View-ViewModel)**: UI層の設計パターン
- **Repository Pattern**: データアクセスの抽象化
- **Service-oriented Architecture**: 音楽再生サービス
- **Dependency Injection**: Koinによる依存性の注入

### 2. アーキテクチャレイヤー

#### 2.1 Presentation Layer (UI層)
```kotlin
// アクティビティ階層
AbsThemeActivity
├── AbsBaseActivity
    ├── AbsMusicServiceActivity
        ├── AbsSlidingMusicPanelActivity
            └── MainActivity
```

**主要コンポーネント:**
- **MainActivity**: ナビゲーションコントローラーとボトムナビゲーションの管理
- **Fragments**: 各機能画面（Home, Songs, Albums, Artists, Playlists等）
- **ViewModels**: UIロジックとデータバインディング

#### 2.2 Domain Layer (ドメイン層)
- **Repository Interface**: データアクセスの抽象化
- **Use Cases**: ビジネスロジックの実装
- **Models**: ドメインオブジェクト

#### 2.3 Data Layer (データ層)
- **Repository Implementation**: 実際のデータアクセス実装
- **Database (Room)**: ローカルデータベース
- **Network**: API通信（Retrofit）

### 3. 依存性注入（Koin）

#### 3.1 DIモジュール構成
```kotlin
// MainModule.kt
val appModules = listOf(
    dataModule,      // Repository実装
    viewModules,     // ViewModel
    roomModule,      // Database DAO
    networkModule,   // Network services
    autoModule       // Android Auto support
)
```

#### 3.2 データフロー
```
UI (Fragments) → ViewModels → Repository → Data Sources
                     ↓
Service Layer (MusicService) ← MusicPlayerRemote
```

## コアコンポーネント

### 1. 音楽再生サービス

#### 1.1 MusicService
- **役割**: 音楽再生の核となるサービス
- **機能**: 
  - メディア再生・停止・スキップ
  - 通知とメディアセッション管理
  - オーディオフォーカス管理
  - Bluetooth接続対応

#### 1.2 MusicPlayerRemote
- **役割**: サービスとUIの仲介
- **パターン**: Singleton
- **機能**: サービスとのインターフェース提供

### 2. データ管理

#### 2.1 Room Database
```kotlin
@Database(entities = [
    PlaylistEntity::class,
    SongEntity::class,
    HistoryEntity::class,
    PlayCountEntity::class,
    // ...
])
abstract class RetroDatabase : RoomDatabase
```

#### 2.2 Repository Pattern
```kotlin
interface Repository {
    suspend fun songs(): List<Song>
    suspend fun albums(): List<Album>
    suspend fun artists(): List<Artist>
    // ...
}
```

### 3. ナビゲーション

#### 3.1 Navigation Component
- **グラフ**: `/res/navigation/main_graph.xml`
- **主要デスティネーション**:
  - Home, Songs, Albums, Artists, Playlists, Genres, Folders, Search

#### 3.2 フラグメント階層
```kotlin
AbsMusicServiceFragment
├── AbsMainActivityFragment
    ├── HomeFragment
    ├── SongsFragment
    ├── AlbumsFragment
    ├── ArtistsFragment
    └── PlaylistsFragment
```

## 主要機能

### 1. 音楽プレイヤー機能
- **複数のプレイヤーテーマ**: 15種類以上のUIテーマ
- **スライディングパネル**: 折りたたみ可能なプレイヤーインターフェース
- **キュー管理**: 再生キューとシャッフル・リピート機能
- **再生制御**: 再生・一時停止・スキップ・シーク・音量調整

### 2. ライブラリ管理
- **メディア整理**: 楽曲・アルバム・アーティスト・プレイリスト・ジャンル別
- **検索機能**: 全メディアタイプ横断検索
- **プレイリスト管理**: カスタムプレイリストの作成・編集・削除
- **お気に入り機能**: Roomデータベースベースのお気に入り管理

### 3. 高度な機能
- **Android Auto連携**: 車載システム対応
- **Cast対応**: Google Cast統合
- **ウィジェット**: 複数スタイルのアプリウィジェット
- **動的ショートカット**: クイックアクセス機能
- **テーマ**: Material Design 3とダイナミックカラー

## 技術スタック

### 1. 開発言語・フレームワーク
- **Kotlin**: 100% Kotlinコードベース
- **Android SDK**: Android 21+ (API Level 21以上)
- **Java Version**: OpenJDK 21

### 2. 主要ライブラリ
```gradle
// UI & Navigation
implementation 'androidx.navigation:navigation-fragment-ktx'
implementation 'androidx.navigation:navigation-ui-ktx'
implementation 'com.google.android.material:material'

// Database
implementation 'androidx.room:room-runtime'
implementation 'androidx.room:room-ktx'

// Image Loading
implementation 'com.github.bumptech.glide:glide'

// Dependency Injection
implementation 'io.insert-koin:koin-android'

// Network
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android'
```

### 3. ビルドシステム
- **Gradle**: Android Gradle Plugin 8.x系
- **KSP**: Kotlin Symbol Processing
- **Flavors**: `normal` (Google Play版), `fdroid` (F-Droid版)

### 4. 品質管理
- **ProGuard**: コード難読化・最適化
- **Lint**: 静的解析
- **Spotless**: コードフォーマット

## パフォーマンス最適化

### 1. メモリ管理
- **WeakReference**: サービス接続での弱参照使用
- **適切なスコープ管理**: Coroutineスコープの適切な管理
- **リソース最適化**: 使用後のリソース解放

### 2. UI/UX最適化
- **ViewBinding**: タイプセーフなビュー参照
- **RecyclerView**: 効率的なリスト表示
- **イメージローダー**: Glideによる効率的な画像読み込み

### 3. バックグラウンド処理
- **Coroutines**: 非同期処理とバックグラウンド処理
- **WorkManager**: 長時間実行タスクの管理
- **Service**: 音楽再生の継続性確保

## セキュリティ・権限

### 1. 必要な権限
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 2. セキュリティ対策
- **ProGuard**: リリースビルドでの難読化
- **権限の最小化**: 必要最小限の権限のみ要求
- **安全なファイルアクセス**: Scoped Storageの適切な使用

## テスト戦略

### 1. テストタイプ
- **Unit Tests**: ロジックの単体テスト
- **Integration Tests**: コンポーネント間の統合テスト
- **UI Tests**: エンドツーエンドテスト

### 2. テストツール
- **JUnit**: 単体テスト
- **Mockk**: モッキングライブラリ
- **Room Testing**: データベーステスト

## 国際化対応

### 1. 多言語サポート
- **リソース分離**: 言語別リソースファイル
- **サポート言語**: 30言語以上
- **Crowdin**: 翻訳管理プラットフォーム

### 2. 地域対応
- **日付・時刻**: 地域別フォーマット
- **数値**: 地域別数値表示
- **通貨**: 地域別通貨表示

## 継続的インテグレーション

### 1. CI/CD
- **Fastlane**: 自動化されたビルドとデプロイ
- **GitHub Actions**: 継続的インテグレーション
- **自動テスト**: プルリクエスト時の自動テスト実行

### 2. リリース管理
- **版数管理**: セマンティックバージョニング
- **変更履歴**: 自動生成される変更ログ
- **段階的リリース**: Google Play段階的展開

## まとめ

RetroMusicPlayerは、モダンなAndroid開発のベストプラクティスを活用し、保守性とスケーラビリティを重視した設計となっています。レイヤード・アーキテクチャとMVVMパターンの組み合わせにより、各層の責任が明確に分離され、テストしやすく拡張しやすい構造となっています。

このアーキテクチャにより、新機能の追加や既存機能の改善が容易になり、長期的な保守性が確保されています。