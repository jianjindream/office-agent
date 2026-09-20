# DreamLoop Web

DreamLoop 的 Vue 3 前端工程。当前阶段完成了产品级视觉骨架、主导航、主题系统、响应式布局以及 API 基础层。

## 本地开发

```bash
npm install
npm run dev
```

开发服务器默认运行在 `http://localhost:5173`，并将 `/api` 请求代理到 `http://localhost:8090`。

## 构建

```bash
npm run build
```

构建结果输出到 `dist/`。当前旧版页面仍保留在 Spring Boot 的 `src/main/resources/static/index.html`，生产集成将在后续阶段完成。

## 目录约定

- `src/api`：统一 HTTP 请求与后端接口
- `src/components`：跨页面组件和基础 UI
- `src/layouts`：应用级布局
- `src/pages`：路由页面
- `src/router`：路由定义
- `src/stores`：Pinia 状态
- `src/types`：接口和领域类型

## 当前页面

- `/chat`：AI 对话工作台
- `/knowledge`：知识库
- `/documents`：AI 文档
- `/tools`：工具中心
- `/settings`：工作区设置
