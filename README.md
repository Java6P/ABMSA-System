# 🎯 ABMSA-System - 方面级多模态情感分析系统

**Aspect-Based Multimodal Sentiment Analysis System**

> 一个面向社交媒体推文的方面级多模态情感分析系统，支持文本+图像的多模态输入，实现对指定目标（Aspect）的情感极性预测。同时集成关键词监测与定时爬虫功能，实现舆情数据的自动化采集与分析。

## 📖 项目简介

本系统的主要功能包括：

1. **方面级情感分析**：对社交媒体推文进行目标级别的情感分析（正面/负面/中性）
2. **关键词监测**：用户可配置监测关键词，系统通过后台爬虫定时采集相关推文数据
3. **目标提取与子目标配置**：支持用户自定义分析子目标，也支持系统自动从推文中提取分析目标
4. **舆情可视化**：对分析结果进行多维度可视化展示

> ⚠️ **模型服务**（Python Flask 推理接口）作为独立项目维护，本系统通过 HTTP 接口调用模型服务完成情感预测与目标提取。

### 核心概念说明

| 概念                   | 说明                                                                      |
| -------------------- | ----------------------------------------------------------------------- |
| **关键词 (Keyword)**    | 用户配置的监测词，用于爬虫定时采集相关推文                                                   |
| **子目标 (Sub-Target)** | 用户在关键词下配置的具体分析目标，如关键词为 "Apple" 时，子目标可以是 "iPhone"、"MacBook"、"Tim Cook" 等 |
| **目标 (Target)**      | 情感分析的对象，**不是推文固有属性**，而是分析过程中确定的。如果用户配置了子目标则按子目标逐一分析；若未配置，则由系统自动从推文内容中提取 |

### 输入输出示例

**输入（推文原始数据）：**

```
Tweet: RT @bohemianizm : Art from Rachael Speirs #art #visualart #craft
Image: [可选的推文配图]
```

**目标确定过程：**

```
情况一：用户配置了子目标 → ["Rachael Speirs", "bohemianizm"]
  → 对每个子目标分别进行情感分析，产生 2 条分析记录

情况二：用户未配置子目标 → 系统自动提取
  → CrawlerService 目标提取 → 识别出 "Rachael Speirs"
  → 对提取出的目标进行情感分析
```

**输出（分析结果）：**

```json
{
  "target": "Rachael Speirs",
  "sentiment": "positive",
  "confidence": 0.87,
  "probabilities": {
    "positive": 0.87,
    "neutral": 0.10,
    "negative": 0.03
  }
}
```

## 🏗️ 系统架构

```
┌──────────────────────────────────────────────────────────────┐
│                       前端 (Frontend)                        │
│                 Vue 3 + Element Plus + ECharts                │
│                                                              │
│  ┌────────┐ ┌────────┐ ┌──────────┐ ┌────────┐ ┌─────────┐  │
│  │情感分析 │ │批量分析 │ │关键词监测 │ │历史记录 │ │可视化大屏│  │
│  └────────┘ └────────┘ └──────────┘ └────────┘ └─────────┘  │
└───────────────────────────┬──────────────────────────────────┘
                            │ HTTP / REST API
┌───────────────────────────▼──────────────────────────────────┐
│                      后端 (Backend)                           │
│                    Spring Boot 3                              │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐    │
│  │                    业务模块                            │    │
│  │  ┌──────────┐ ┌──────────┐ ┌────────┐ ┌───────────┐ │    │
│  │  │ 情感分析  │ │ 关键词管理│ │用户管理 │ │ 历史记录  │ │    │
│  │  └──────────┘ └──────────┘ └────────┘ └───────────┘ │    │
│  │  ┌──────────────────────────────────────────────────┐│    │
│  │  │       子目标管理 + 目标提取 (CrawlerService)      ││    │
│  │  └──────────────────────────────────────────────────┘│    │
│  └──────────────────────────────────────────────────────┘    │
│  ┌──────────────────────────────────────────────────────┐    │
│  │                    定时任务模块                        │    │
│  │  ┌──────────────┐  ┌─────────────────────────────┐   │    │
│  │  │ 关键词爬虫调度 │  │ 爬取 → 目标确定 → 分析 → 入库│   │    │
│  │  │ (Scheduled)   │  │ 更新 is_analyzed 状态       │   │    │
│  │  └──────────────┘  └─────────────────────────────┘   │    │
│  └──────────────────────────────────────────────────────┘    │
└──────────┬──────────────────────────────────┬────────────────┘
           │ HTTP 调用                         │ JDBC
┌──────────▼────────────┐          ┌───────────▼───────────────┐
│   模型服务 (独立项目)   │          │        MySQL 数据库        │
│   Python Flask API    │          │                           │
│                       │          │  ┌─────────┐ ┌─────────┐  │
│  POST /api/predict    │          │  │ 关键词表 │ │ 子目标表 │  │
│  POST /api/predict/   │          │  │ 推文表   │ │ 分析记录 │  │
│       batch           │          │  │ 监测任务 │ │ 爬虫日志 │  │
│  POST /api/extract-   │          │  └─────────┘ └─────────┘  │
│       targets         │          └───────────────────────────┘
└───────────────────────┘
```

## 📁 项目结构

```
ABMSA-System/
│
├── frontend/                          # 前端项目 (Vue 3)
│   ├── src/
│   │   ├── api/                       # API 接口封装
│   │   │   ├── analysis.js            # 情感分析接口
│   │   │   ├── keyword.js             # 关键词监测接口
│   │   │   ├── subtarget.js           # 子目标管理接口
│   │   │   ├── history.js             # 历史记录接口
│   │   │   └── user.js                # 用户相关接口
│   │   ├── assets/                    # 静态资源
│   │   ├── components/                # 公共组件
│   │   │   ├── SentimentInput.vue     # 情感分析输入组件
│   │   │   ├── ResultDisplay.vue      # 结果展示组件
│   │   │   ├── ImageUploader.vue      # 图片上传组件
│   │   │   ├── KeywordTag.vue         # 关键词标签组件
│   │   │   ├── SubTargetConfig.vue    # 子目标配置组件
│   │   │   └── SentimentChart.vue     # 情感分布图表组件
│   │   ├── views/                     # 页面视图
│   │   │   ├── HomeView.vue           # 首页
│   │   │   ├── AnalysisView.vue       # 单条分析页
│   │   │   ├── BatchView.vue          # 批量分析页
│   │   │   ├── KeywordMonitor.vue     # 关键词监测页（含子目标配置）
│   │   │   ├── MonitorDetail.vue      # 监测详情页
│   │   │   ├── HistoryView.vue        # 历史记录页
│   │   │   └── DashboardView.vue      # 数据可视化大屏
│   │   ├── router/
│   │   │   └── index.js
│   │   ├── store/
│   │   │   ├── analysis.js
│   │   │   └── keyword.js
│   │   ├── utils/
│   │   │   └── request.js             # Axios 封装
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
│
├── backend/                           # 后端项目 (Spring Boot)
│   ├── src/main/java/com/abmsa/
│   │   ├── controller/
│   │   │   ├── AnalysisController.java
│   │   │   ├── KeywordController.java
│   │   │   ├── SubTargetController.java      # 🆕 子目标管理
│   │   │   ├── MonitorTaskController.java
│   │   │   ├── HistoryController.java
│   │   │   └── UserController.java
│   │   ├── service/
│   │   │   ├── AnalysisService.java
│   │   │   ├── ModelCallService.java         # 调用 Flask 模型接口
│   │   │   ├── KeywordService.java
│   │   │   ├── SubTargetService.java         # 子目标 CRUD
│   │   │   ├── CrawlerService.java           # 含目标提取逻辑
│   │   │   ├── TargetExtractService.java     # 目标提取服务
│   │   │   ├── MonitorTaskService.java
│   │   │   ├── HistoryService.java
│   │   │   └── UserService.java
│   │   ├── task/
│   │   │   ├── KeywordCrawlTask.java         # 定时爬取调度（含 is_analyzed 更新）
│   │   │   └── AutoAnalysisTask.java         # 自动分析任务
│   │   ├── entity/
│   │   │   ├── Keyword.java
│   │   │   ├── SubTarget.java                # 子目标实体
│   │   │   ├── MonitorTask.java
│   │   │   ├── CrawledTweet.java
│   │   │   ├── AnalysisRecord.java           # 🔄 tweet_id → crawled_tweet_id
│   │   │   ├── CrawlLog.java
│   │   │   └── User.java
│   │   ├── mapper/
│   │   ├── dto/
│   │   ├── config/
│   │   │   ├── ScheduleConfig.java
│   │   │   ├── RestTemplateConfig.java
│   │   │   └── CorsConfig.java
│   │   └── AbmsaApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── mapper/
│   ├── sql/
│   │   └── init.sql
│   └── pom.xml
│
├── docs/
│   ├── API.md
│   ├── DEPLOYMENT.md
│   ├── DATABASE.md
│   └── screenshots/
│
├── docker-compose.yml
├── .gitignore
└── README.md
```

## 🗄️ 数据库设计

### E-R 关系概览

```
User(1) ──────< Keyword(N)
                   │
                   ├── 1:N ──> SubTarget(N)      ← 用户配置的子目标
                   │
                   └── 1:N ──> MonitorTask(N)
                                   │
                                   │ 1:N
                                   ▼
                              CrawledTweet(N)     ← 推文本身不含 target
                                   │
                                   │ 1:N          ← 一条推文可对应多个目标的分析
                                   ▼
                            AnalysisRecord(N)     ← target 在此确定
```

### 核心表结构

#### `t_keyword` - 关键词表

| 字段                | 类型           | 说明              |
| ----------------- | ------------ | --------------- |
| id                | BIGINT       | 主键              |
| user\_id          | BIGINT       | 所属用户            |
| keyword           | VARCHAR(255) | 监测关键词           |
| status            | TINYINT      | 状态（0-停用 / 1-启用） |
| cron\_expression  | VARCHAR(50)  | 定时表达式           |
| last\_crawl\_time | DATETIME     | 上次爬取时间          |
| created\_at       | DATETIME     | 创建时间            |
| updated\_at       | DATETIME     | 更新时间            |

#### `t_sub_target` - 子目标表 🆕

| 字段           | 类型           | 说明                           |
| ------------ | ------------ | ---------------------------- |
| id           | BIGINT       | 主键                           |
| keyword\_id  | BIGINT       | 关联关键词                        |
| target\_name | VARCHAR(255) | 子目标名称（如 "iPhone"、"Tim Cook"） |
| description  | VARCHAR(500) | 子目标描述（可选）                    |
| status       | TINYINT      | 状态（0-停用 / 1-启用）              |
| created\_at  | DATETIME     | 创建时间                         |

> **说明**：子目标是用户在关键词下配置的具体分析对象。例如关键词 "Apple" 下可��置子目标 "iPhone"、"MacBook"。当爬取到关于 "Apple" 的推文后，系统会用每个启用的子目标分别对推文进行情感分析。

#### `t_monitor_task` - 监测任务表

| 字段              | 类型          | 说明                                        |
| --------------- | ----------- | ----------------------------------------- |
| id              | BIGINT      | 主键                                        |
| keyword\_id     | BIGINT      | 关联关键词                                     |
| status          | TINYINT     | 任务状态（0-待执行 / 1-爬取中 / 2-分析中 / 3-完成 / 4-失败） |
| crawled\_count  | INT         | 本次爬取数量                                    |
| analyzed\_count | INT         | 已分析数量                                     |
| target\_source  | VARCHAR(20) | 目标来源（USER\_CONFIG / SYSTEM\_EXTRACT）      |
| start\_time     | DATETIME    | 开始时间                                      |
| end\_time       | DATETIME    | 结束时间                                      |
| error\_msg      | TEXT        | 失败时的错误信息                                  |

#### `t_crawled_tweet` - 爬取推文表

| 字段               | 类型           | 说明                         |
| ---------------- | ------------ | -------------------------- |
| id               | BIGINT       | 主键                         |
| task\_id         | BIGINT       | 关联任务                       |
| keyword\_id      | BIGINT       | 关联关键词                      |
| tweet\_id        | VARCHAR(64)  | 推文原始 ID                    |
| content          | TEXT         | 推文内容                       |
| image\_url       | VARCHAR(500) | 推文配图 URL                   |
| author           | VARCHAR(128) | 推文作者                       |
| publish\_time    | DATETIME     | 发布时间                       |
| crawled\_at      | DATETIME     | 爬取时间                       |
| **is\_analyzed** | **TINYINT**  | **是否已完成分析（0-未分析 / 1-已分析）** |

> ⚠️ **注意**：`t_crawled_tweet` 中**没有 target 字段**。推文本身不包含分析目标，target 是在分析阶段确定的，记录在 `t_analysis_record` 中。

#### `t_analysis_record` - 分析记录表

| 字段                     | 类型               | 说明                                                       |
| ---------------------- | ---------------- | -------------------------------------------------------- |
| id                     | BIGINT           | 主键                                                       |
| **crawled\_tweet\_id** | **BIGINT**       | **关联爬取推文（可为空，手动输入时为空）**                                  |
| user\_id               | BIGINT           | 所属用户                                                     |
| source                 | TINYINT          | 来源（0-手动输入 / 1-关键词爬取）                                     |
| **target**             | **VARCHAR(255)** | **分析目标（分析过程中确定）**                                        |
| **target\_source**     | **VARCHAR(20)**  | **目标来源（USER\_CONFIG / SYSTEM\_EXTRACT / MANUAL\_INPUT）** |
| content                | TEXT             | 推文内容                                                     |
| image\_url             | VARCHAR(500)     | 图片地址                                                     |
| sentiment              | VARCHAR(20)      | 情感结果（positive / neutral / negative）                      |
| confidence             | DECIMAL(5,4)     | 置信度                                                      |
| prob\_positive         | DECIMAL(5,4)     | 正面概率                                                     |
| prob\_neutral          | DECIMAL(5,4)     | 中性概率                                                     |
| prob\_negative         | DECIMAL(5,4)     | 负面概率                                                     |
| created\_at            | DATETIME         | 分析时间                                                     |

> **关键设计**：一条 `CrawledTweet` 可以对应**多条** `AnalysisRecord`（当有多个子目标时，每个子目标生成一条分析记录）。

## 🎯 目标确定机制

这是本系统的核心设计之一——**target（分析目标）不是推文的固有属性，而是在分析过程中确定的。**

### 流程图

```
                    爬取到新推文
                        │
                        ▼
              ┌───────────────────┐
              │ 该关键词是否配置了  │
              │    子目标？         │
              └──────────┬─────────┘
                   ╱         ╲
                 是             否
                ╱                 ╲
               ▼                   ▼
    ┌──────────────────┐  ┌──────────────────────┐
    │ 获取所有启用的     │  │ 调用目标提取服务       │
    │ 子目标列表        │  │ TargetExtractService  │
    │                  │  │                      │
    │ 如: ["iPhone",   │  │ 策略一: 规则提取       │
    │  "MacBook",      │  │  - @提及 → 提取用户名  │
    │  "Tim Cook"]     │  │  - 人名/地名识别       │
    │                  │  │  - 关键词匹配          │
    │                  │  │                      │
    │                  │  │ 策略二: 模型提取       │
    │                  │  │  - 调用 Flask 接口     │
    │                  │  │  POST /api/extract-   │
    │                  │  │       targets         │
    └────────┬─────────┘  └──────────┬───────────┘
             │                       │
             ▼                       ▼
    targets = 用户子目标      targets = 系统提取结果
             │                       │
             └───────────┬───────────┘
                         ▼
              ┌─────────────────────┐
              │ 遍历 targets 列表    │
              │ 对每个 target 调用    │
              │ 模型服务进行情感分析   │
              │ POST /api/predict    │
              └─────────┬───────────┘
                        │
                        ▼
              ┌─────────────────────┐
              │ 每个 target 生成一条  │
              │ AnalysisRecord 记录  │
              │ 记录 target_source:  │
              │ USER_CONFIG 或       │
              │ SYSTEM_EXTRACT       │
              └─────────┬───────────┘
                        │
                        ▼
              ┌──────────────────────┐
              │ 更新 CrawledTweet    │
              │ is_analyzed = 1     │
              └─────────────────────┘
```

### TargetExtractService 目标提取策略

```java
@Service
public class TargetExtractService {

    @Autowired
    private ModelCallService modelCallService;

    /**
     * 从推文内容中提取分析目标
     * 策略优先级: 规则提取 → 模型提取 → 关键词本身兜底
     */
    public List<String> extractTargets(String content, String keyword) {
        List<String> targets = new ArrayList<>();

        // 策略一: 基于规则的提取
        targets.addAll(extractByRules(content));

        // 策略二: 如果规则未提取到，调用模型服务提取
        if (targets.isEmpty()) {
            targets.addAll(extractByModel(content));
        }

        // 兜底: 如果仍然为空，使用关键词本身作为目标
        if (targets.isEmpty()) {
            targets.add(keyword);
        }

        return targets;
    }

    /**
     * 规则提取: 从推文中按规则识别可能的目标
     */
    private List<String> extractByRules(String content) {
        List<String> targets = new ArrayList<>();

        // 规则1: 提取 @提及 的用户名
        Pattern mentionPattern = Pattern.compile("@(\\w+)");
        Matcher matcher = mentionPattern.matcher(content);
        while (matcher.find()) {
            targets.add(matcher.group(1));
        }

        // 规则2: 提取 #话题标签
        Pattern hashtagPattern = Pattern.compile("#(\\w+)");
        matcher = hashtagPattern.matcher(content);
        while (matcher.find()) {
            targets.add(matcher.group(1));
        }

        // 规则3: 更多自定义规则可在此扩展...

        return targets;
    }

    /**
     * 模型提取: 调用 Flask 模型服务的目标提取接口
     */
    private List<String> extractByModel(String content) {
        return modelCallService.extractTargets(content);
    }
}
```

## 🔑 关键词监测 & 定时任务

### 功能流程

```
┌──────────┐    ┌────────────┐    ┌────────────┐    ┌───────────────┐
│ 用户添加  │    │ 配置子目标  │    │ 定时任务    │    │ 读取启用的    │
│ 监测关键词│───▶│（可选）    │───▶│ Cron 触发  │───▶│ 关键词       │
└──────────┘    └────────────┘    └────────────┘    └───────┬───────┘
                                                            │
  ┌──────────────┐    ┌──────────────┐    ┌─────────────────▼──────┐
  │ 前端展示结果  │◀───│ 批量写入      │◀───│ 调用爬虫，推文入库      │
  │ 按目标维度统计│    │ AnalysisRecord│    │ CrawledTweet          │
  └──────────────┘    └──────┬───────┘    └─────────────────┬──────┘
                             │                              │
                             │    ┌─────────────────────┐   │
                             │◀───│ 确定 targets:       │◀──┘
                                  │ 有子目标 → 用子目标   │
                                  │ 无子目标 → 系统提取   │
                                  └──────────┬──────────┘
                                             │
                                  ┌──────────▼──────────┐
                                  │ 逐目标调用模型服务    │
                                  │ 更新 is_analyzed = 1 │
                                  └─────────────────────┘
```

### 定时任务核心逻辑

```java
/**
 * 关键词定时爬取 + 自动分析任务
 */
@Scheduled(cron = "${crawler.schedule.cron}")
public void executeKeywordCrawl() {
    // 1. 查询所有启用状态的关键词
    List<Keyword> keywords = keywordService.listEnabled();

    for (Keyword kw : keywords) {
        MonitorTask task = null;
        try {
            // 2. 创建监测任务记录
            task = monitorTaskService.createTask(kw.getId());
            monitorTaskService.updateStatus(task.getId(), TaskStatus.CRAWLING);

            // 3. 触发爬虫 → 爬取的数据写入 t_crawled_tweet（is_analyzed = 0）
            crawlerService.triggerCrawl(kw.getKeyword(), task.getId(), kw.getId());

            // 4. 获取本次爬取的未分析推文
            monitorTaskService.updateStatus(task.getId(), TaskStatus.ANALYZING);
            List<CrawledTweet> tweets = crawledTweetService.listUnanalyzed(task.getId());

            // 5. 确定分析目标
            List<SubTarget> subTargets = subTargetService.listEnabled(kw.getId());
            String targetSource;

            if (!subTargets.isEmpty()) {
                // ✅ 用户配置了子目标 → 按子目标分析
                targetSource = "USER_CONFIG";
            } else {
                // ✅ 未配置子目标 → 系统自动提取
                targetSource = "SYSTEM_EXTRACT";
            }
            task.setTargetSource(targetSource);

            int totalAnalyzed = 0;

            for (CrawledTweet tweet : tweets) {
                // 6. 确定当前推文的 targets 列表
                List<String> targets;
                if ("USER_CONFIG".equals(targetSource)) {
                    targets = subTargets.stream()
                        .map(SubTarget::getTargetName)
                        .collect(Collectors.toList());
                } else {
                    targets = targetExtractService.extractTargets(
                        tweet.getContent(), kw.getKeyword()
                    );
                }

                // 7. 对每个 target 调用模型服务进行情感分析
                for (String target : targets) {
                    AnalysisRecord record = modelCallService.predict(
                        target, tweet.getContent(), tweet.getImageUrl()
                    );
                    record.setCrawledTweetId(tweet.getId());
                    record.setTargetSource(targetSource);
                    record.setSource(1); // 来源：关键词爬取
                    analysisRecordService.save(record);
                    totalAnalyzed++;
                }

                // 8. ✅ 更新推文的 is_analyzed 状态
                crawledTweetService.markAsAnalyzed(tweet.getId());
            }

            // 9. 完成任务
            monitorTaskService.completeTask(
                task.getId(), tweets.size(), totalAnalyzed
            );
            keywordService.updateLastCrawlTime(kw.getId());

        } catch (Exception e) {
            if (task != null) {
                monitorTaskService.failTask(task.getId(), e.getMessage());
            }
            log.error("关键词 [{}] 爬取分析失败", kw.getKeyword(), e);
        }
    }
}
```

## 📡 API 接口说明

### 情感分析接口

| 方法   | 路径                      | 描述                          |
| ---- | ----------------------- | --------------------------- |
| POST | `/api/analysis/single`  | 单条情感分析（手动输入 target + tweet） |
| POST | `/api/analysis/batch`   | 批量情感分析（上传文件）                |
| GET  | `/api/analysis/history` | 获取分析历史列表                    |
| GET  | `/api/analysis/{id}`    | 获取分析详情                      |
| GET  | `/api/analysis/export`  | 导出分析结果                      |

### 关键词监测接口

| 方法     | 路径                         | 描述       |
| ------ | -------------------------- | -------- |
| GET    | `/api/keyword/list`        | 获取关键词列表  |
| POST   | `/api/keyword/add`         | 添加监测关键词  |
| PUT    | `/api/keyword/{id}`        | 修改关键词配置  |
| PUT    | `/api/keyword/{id}/toggle` | 启用/停用关键词 |
| DELETE | `/api/keyword/{id}`        | 删除关键词    |

### 子目标管理接口 🆕

| 方法     | 路径                                     | 描述           |
| ------ | -------------------------------------- | ------------ |
| GET    | `/api/keyword/{keywordId}/sub-targets` | 获取关键词下的子目标列表 |
| POST   | `/api/keyword/{keywordId}/sub-targets` | 添加子目标        |
| PUT    | `/api/sub-target/{id}`                 | 修改子目标        |
| PUT    | `/api/sub-target/{id}/toggle`          | 启用/停用子目标     |
| DELETE | `/api/sub-target/{id}`                 | 删除子目标        |

### 监测任务接口

| 方法   | 路径                                 | 描述        |
| ---- | ---------------------------------- | --------- |
| GET  | `/api/monitor/tasks`               | 获取监测任务列表  |
| GET  | `/api/monitor/task/{id}`           | 获取任务详情    |
| GET  | `/api/monitor/task/{id}/tweets`    | 获取任务爬取的推文 |
| GET  | `/api/monitor/task/{id}/results`   | 获取任务的分析结果 |
| POST | `/api/monitor/trigger/{keywordId}` | 手动触发一次爬取  |
| GET  | `/api/monitor/logs`                | 爬虫日志查询    |

### 统计与可视化接口

| 方法  | 路径                                          | 描述         |
| --- | ------------------------------------------- | ---------- |
| GET | `/api/dashboard/overview`                   | 系统总览统计     |
| GET | `/api/dashboard/sentiment-trend`            | 情感趋势（按时间）  |
| GET | `/api/dashboard/keyword-stats/{id}`         | 单个关键词的情感统计 |
| GET | `/api/dashboard/target-compare/{keywordId}` | 子目标间情感对比   |
| GET | `/api/dashboard/word-cloud/{keywordId}`     | 词云数据       |

### 用户接口

| 方法   | 路径                   | 描述     |
| ---- | -------------------- | ------ |
| POST | `/api/user/login`    | 用户登录   |
| POST | `/api/user/register` | 用户注册   |
| GET  | `/api/user/info`     | 获取用户信息 |

### 模型服务接口（独立项目，Flask 提供）

| 方法   | 路径                     | 描述                 |
| ---- | ---------------------- | ------------------ |
| POST | `/api/predict`         | 单条情感预测（需传入 target） |
| POST | `/api/predict/batch`   | 批量情感预测             |
| POST | `/api/extract-targets` | 从推文中自动提取目标         |
| GET  | `/api/health`          | 模型服务健康检查           |

#### POST `/api/extract-targets`（模型服务提供）

```json
// 请求
{ "content": "RT @bohemianizm : Art from Rachael Speirs #art #visualart #craft" }

// 响应
{
  "code": 200,
  "data": {
    "targets": ["Rachael Speirs", "bohemianizm"]
  }
}
```

## 🚀 快速开始

### 环境要求

| 组件      | 版本要求    |
| ------- | ------- |
| Node.js | >= 18.0 |
| Java    | >= 17   |
| MySQL   | >= 8.0  |
| Maven   | >= 3.8  |

> 模型服务（Python Flask）请参考独立项目的文档进行部署。

### 1. 克隆项目

```bash
git clone https://github.com/Java6P/ABMSA-System.git
cd ABMSA-System
```

### 2. 初始化数据库

```bash
mysql -u root -p < backend/sql/init.sql
```

### 3. 配置后端

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/abmsa?useUnicode=true&characterEncoding=utf-8
    username: root
    password: your_password

# 模型服务地址（独立部署的 Flask 服务）
model:
  service:
    url: http://localhost:5000

# 爬虫定时任务配置
crawler:
  schedule:
    cron: "0 0 */1 * * ?"   # 每小时执行一次
  enabled: true

# 目标提取配置
target:
  extract:
    strategy: RULE_FIRST     # RULE_FIRST | MODEL_FIRST | MODEL_ONLY
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
# 后端服务运行在 http://localhost:8080
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:5173
```

### 6. Docker 一键启动（推荐）

```bash
docker-compose up -d
```

## 🖥️ 系统功能总览

| 模块       | 功能    | 说明                                          |
| -------- | ----- | ------------------------------------------- |
| 🔍 情感分析  | 单条分析  | 手动输入 Target + Tweet + 可选图片，实时返回结果           |
| 🔍 情感分析  | 批量分析  | 上传 CSV/Excel，批量分析并导出                        |
| 🔑 关键词监测 | 关键词管理 | 添加/编辑/启用/停用监测关键词                            |
| 🎯 子目标管理 | 子目标配置 | 为关键词配置具体的分析目标（如产品名、人名等）                     |
| 🎯 目标提取  | 自动提取  | 未配置子目标时，系统自动从推文中提取分析目标                      |
| 🔑 关键词监测 | 定时爬取  | 后台定时爬取关键词相关推文                               |
| 🔑 关键词监测 | 自动分析  | 爬取推文 → 确定目标 → 调用模型 → 结果入库 → 更新 is\_analyzed |
| 🔑 关键词监测 | 手动触发  | 支持手动触发单个关键词的立即爬取                            |
| 🔑 关键词监测 | 爬虫日志  | 查看每次爬取任务的执行状态和详情                            |
| 📊 可视化   | 情感分布  | 饼图展示正面/负面/中性比例                              |
| 📊 可视化   | 趋势分析  | 折线图展示情感随时间的变化趋势                             |
| 📊 可视化   | 目标对比  | 同一关键词下不同子目标的情感对比                            |
| 📊 可视化   | 词云    | 基于关键词爬取的推文生成词云                              |
| 📋 历史记录  | 记录管理  | 查看所有分析记录（手动 + 自动），支持按目标来源筛选                 |
| 📋 历史记录  | 数据导出  | 支持 CSV / Excel 导出                           |
| 👤 用户管理  | 登录注册  | JWT 认证                                      |

## 🛠️ 技术栈

| 层级       | 技术                         |
| -------- | -------------------------- |
| 前端框架     | Vue 3 + Vite               |
| UI 组件库   | Element Plus               |
| 图表库      | ECharts                    |
| 状态管理     | Pinia                      |
| HTTP 客户端 | Axios                      |
| 后端框架     | Spring Boot 3              |
| ORM      | MyBatis-Plus               |
| 数据库      | MySQL 8.0                  |
| 定时任务     | Spring Task (`@Scheduled`) |
| 接口调用     | RestTemplate / WebClient   |
| 容器化      | Docker + Docker Compose    |
| 模型服务     | Python Flask（独立项目）         |

## 📝 License

本项目仅用于学术研究和学位论文用途。

## 🙏 致谢

- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [ECharts](https://echarts.apache.org/)
- [MyBatis-Plus](https://baomidou.com/)

