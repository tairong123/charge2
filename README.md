# Java Swing 記帳系統

這是一款使用 Java Swing 製作的本地記帳桌面應用程式，支援收入與支出管理、即時資料編輯、圓餅圖收支分析等功能。資料儲存於 SQLite 資料庫，適合個人記帳使用或教學示範。

---

## 功能特色

- 登入 / 註冊系統（多帳號支援）
- 記帳功能（新增、編輯、刪除）
- 年/月/日篩選（預設為當天）
- 表格內即時編輯並同步更新資料庫
- 刪除資料後自動更新年份選單與畫面
- 收入與支出分類統計（圓餅圖 + 百分比）
- 資料保存在本地 SQLite 檔案中

---

## 系統架構

```
src/main/java/org/example/
│
├── MainApp.java                // 主程式入口
│
├── db/
│   └── DatabaseManager.java    // 資料庫初始化與連線管理
│
├── service/
│   └── UserService.java        // 登入與註冊邏輯
│
├── ui/
│   ├── LoginFrame.java         // 登入/註冊視窗
│   ├── MainFrame.java          // 主視窗，含三個分頁
│   ├── TodayPanel.java         // 今日記帳分頁
│   ├── HistoryPanel.java       // 歷史紀錄分頁
│   └── ReportPanel.java        // 收支分析（圓餅圖）分頁
│
├── component/
│   ├── ButtonRenderer.java     // 表格中「刪除」按鈕顯示
│   ├── DeleteButtonEditor.java // 表格中刪除功能（支援 reload）
│   ├── DeleteAction.java       // 刪除策略介面
│   └── RecordDialog.java       // 新增記帳彈出視窗
│
└── util/
    └── InputValidator.java     // 輸入驗證工具（日期/金額/分類）
```

---

## 執行方式

### 1. 安裝環境

- Java 17（建議使用 [Temurin](https://adoptium.net) 版本）
- 支援 IDE：IntelliJ / Eclipse / VS Code

### 2. 編譯與執行

```bash
# 使用 Maven 編譯
mvn clean compile

# 執行主程式
mvn exec:java -Dexec.mainClass="org.example.MainApp"
```

或使用 IntelliJ 點擊 `MainApp` 右鍵 → Run

---

## 資料庫說明

系統會自動建立本地資料庫 `accounting.db`，包含以下兩張表：

- `users`：使用者登入資訊
- `records`：記帳紀錄（類型、類別、金額、日期、備註）

---

## 畫面預覽

| 記帳畫面 | 歷史查詢 | 收支分析 |
|----------|----------|----------|
| 即時新增、編輯 | 年/月篩選、雙向同步 | 圓餅圖統計收入支出比例 |

---

## 使用到的函式庫

- **JFreeChart**：圓餅圖繪製
- **SQLite JDBC**：資料庫連線（無需安裝額外資料庫）
- **Java Swing**：GUI 介面
