这个项目是一个简易版的 Spring 框架实现，具备基本的 IOC 容器、依赖注入（DI）、Bean 生命周期管理以及扩展机制（如 `BeanPostProcessor`）
---

### 1. **IOC 容器核心**
- **ApplicationContext 类**：作为整个容器的核心，负责初始化上下文、扫描包、创建 Bean 并管理其生命周期。
- **单例池 ioc**：使用 `Map<String, Object>` 存储所有已经创建完成的 Bean 实例。
- **beanDefinitionMap**：存储每个 Bean 的定义信息（`BeanDefinition`），包括类名、构造方法、注解信息等。

---

### 2. **Bean 的创建流程**
- **BeanDefinition 类**：描述一个 Bean 的元数据，包含：
    - bean 名称
    - 构造函数
    - `@Autowired` 字段列表
    - `@PostConstruct` 方法

- **createBean / doCreateBean 方法**：负责创建 Bean 实例，并处理依赖注入和初始化逻辑。

---

### 3. **依赖注入（DI）**
- **autowiredBean 方法**：根据 `BeanDefinition` 中收集的 `@Autowired`字段进行依赖注入。
- 使用反射将依赖对象赋值给字段。

---

### 4. **Bean 生命周期管理**
- 支持 `@PostConstruct` 注解标注的方法，在 Bean 初始化时自动调用。
- 提供 `BeanPostProcessor` 接口用于在 Bean 初始化前后插入自定义逻辑。
    - `beforeInitialization()`：初始化前拦截
    - `afterInitialization()`：初始化后拦截

示例实现：`MyBeanPostProcessor` 打印初始化阶段日志。

---

### 5. **组件扫描与自动注册**
- **scanPackage 方法**：递归扫描指定包路径下的 `.class` 文件并加载为 Class 对象。
- 根据 `@Component` 注解判断是否需要注册为 Bean。
- 包名转换为路径格式，支持跨平台兼容性处理。

---

### 6. **获取 Bean 的方式**
- **getBean(String name)**：通过名称获取 Bean 实例。
- **getBean(Class<T> type)**：通过类型获取唯一匹配的 Bean。
- **getBeans(Class<T> type)**：获取该类型的所有 Bean 列表。

---

### 7. **接口设计**
- **BeanPostProcessor 接口**：允许用户介入 Bean 的初始化过程，提供 AOP 基础能力。
- **默认方法实现**：便于用户只覆盖感兴趣的方法。

---

### 总结图示结构
```
ApplicationContext
├── IOC 容器 (ioc)
├── 正在加载的 Bean (loadingIoc)
├── Bean 定义 (beanDefinitionMap)
├── 后置处理器 (postProcessors)
│
├── Bean 创建流程
│   ├── wrapper() -> 创建 BeanDefinition
│   ├── createBean() -> 实例化 + 初始化
│   └── autowiredBean() -> 注入依赖
│       initializeBean() -> 生命周期回调 + PostProcessor
│
└── 组件扫描
    └── scanPackage() -> 加载类 -> 过滤 @Component -> 注册 Bean
```


---

### 特点与可扩展性
- 简洁清晰地模拟了 Spring IOC 核心机制。
- 支持注解驱动开发（`@Component`, `@Autowired`, `@PostConstruct`）。
- 可以轻松扩展：
    - 添加新的 `BeanPostProcessor` 实现来增强功能（如代理、日志、安全控制等）。
    - 支持更多 Spring 功能（如 `@Scope`, `@Qualifier`, 配置类等）逐步加入。
    - 添加新的扫描机制，如 `@Service`, `@Repository`, `@Controller` 等。
    - 添加新的 Bean 定义注解，如 `@Value`, `@Configuration`, `@Bean` 等。
    - 添加新的 Bean 生命周期回调，如 `@PreDestroy`, `@PostConstruct` 等。
---

## 🎯 项目现状总结

| 模块 | 状态 |
|------|------|
| IOC 容器 | ✅ 已实现 |
| 依赖注入（@Autowired） | ✅ 已实现 |
| Bean 生命周期管理（@PostConstruct） | ✅ 已实现 |
| 循环依赖处理 | ✅ 使用 `loadingIoc` 做提前曝光（模拟二级缓存） |
| AOP 支持 | ❌ 尚未实现 |
| 三级缓存 | ❌ 尚未实现 |
| Spring MVC 功能 | ❌ 尚未实现 |

---
这个项目非常适合学习 Spring 底层原理，理解 IOC/DI 和 Bean 生命周期的实现机制