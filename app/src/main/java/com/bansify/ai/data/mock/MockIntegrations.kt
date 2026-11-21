package com.bansify.ai.data.mock

enum class IntegrationCategory(val label: String) {
    GIT("Git & Code"),
    HOSTING("Hosting & Edge"),
    DESIGN("Design & UI"),
    DATABASE("Backend & DB"),
    CLOUD("Cloud & DevOps"),
    WORKFLOW("Issues & Docs"),
    TOOLS("Auth, Pay & Ops")
}

data class DeveloperIntegration(
    val id: String,
    val name: String,
    val category: IntegrationCategory,
    val tagline: String,
    val connectionDetail: String,
    val badgeShort: String
)

object MockIntegrations {
    val catalog: List<DeveloperIntegration> = listOf(
        // Git & Code (4)
        DeveloperIntegration("github", "GitHub", IntegrationCategory.GIT, "Repos, branches, pull requests & actions", "bansify/ai · main", "GH"),
        DeveloperIntegration("gitlab", "GitLab", IntegrationCategory.GIT, "Git repositories & CI/CD pipelines", "gitlab.com/bansify", "GL"),
        DeveloperIntegration("bitbucket", "Bitbucket", IntegrationCategory.GIT, "Team Git repos & Jira-linked PRs", "bitbucket/bansify", "BB"),
        DeveloperIntegration("azure_repos", "Azure Repos", IntegrationCategory.GIT, "Enterprise Git repos & boards", "dev.azure.com/bansify", "AZ"),

        // Hosting & Edge (5)
        DeveloperIntegration("vercel", "Vercel", IntegrationCategory.HOSTING, "Instant preview URLs & edge deploys", "bansify-ai.vercel.app", "VC"),
        DeveloperIntegration("cloudflare", "Cloudflare", IntegrationCategory.HOSTING, "Workers, Pages, R2 & D1 edge runtime", "workers.bansify.dev", "CF"),
        DeveloperIntegration("netlify", "Netlify", IntegrationCategory.HOSTING, "Static sites, functions & deploy previews", "bansify.netlify.app", "NL"),
        DeveloperIntegration("railway", "Railway", IntegrationCategory.HOSTING, "Full-stack services & instant containers", "prod-asia-south1", "RW"),
        DeveloperIntegration("render", "Render", IntegrationCategory.HOSTING, "Web services, cron jobs & managed DB", "bansify-api.onrender", "RN"),

        // Design & UI (3)
        DeveloperIntegration("figma", "Figma", IntegrationCategory.DESIGN, "Design tokens, frames & component specs", "Bansify Design System", "FG"),
        DeveloperIntegration("framer", "Framer", IntegrationCategory.DESIGN, "Interactive marketing sites & motion", "bansify.framer.website", "FR"),
        DeveloperIntegration("storybook", "Storybook", IntegrationCategory.DESIGN, "Isolated UI component catalog", "ui.bansify.dev", "SB"),

        // Backend & DB (5)
        DeveloperIntegration("supabase", "Supabase", IntegrationCategory.DATABASE, "Postgres, Auth, Storage & Realtime", "db-ap-south-1", "SP"),
        DeveloperIntegration("firebase", "Firebase", IntegrationCategory.DATABASE, "Firestore, Auth, Crashlytics & App Check", "bansify-prod", "FB"),
        DeveloperIntegration("neon", "Neon", IntegrationCategory.DATABASE, "Serverless branching Postgres database", "branch: main", "NN"),
        DeveloperIntegration("mongodb", "MongoDB Atlas", IntegrationCategory.DATABASE, "Document clusters & vector search", "cluster0-mumbai", "MG"),
        DeveloperIntegration("planetscale", "PlanetScale", IntegrationCategory.DATABASE, "Scalable MySQL with schema branches", "bansify-core", "PS"),

        // Cloud & DevOps (4)
        DeveloperIntegration("aws", "AWS", IntegrationCategory.CLOUD, "S3, Lambda, EC2 & CloudFront", "ap-south-1", "AW"),
        DeveloperIntegration("gcp", "Google Cloud", IntegrationCategory.CLOUD, "Cloud Run, Artifact Registry & Pub/Sub", "bansify-gcp", "GC"),
        DeveloperIntegration("docker", "Docker Hub", IntegrationCategory.CLOUD, "Container images & registry builds", "bansify/app:latest", "DK"),
        DeveloperIntegration("kubernetes", "Kubernetes", IntegrationCategory.CLOUD, "Cluster workloads, pods & ingress", "prod-gke-cluster", "K8"),

        // Issues & Docs (4)
        DeveloperIntegration("linear", "Linear", IntegrationCategory.WORKFLOW, "Issues, cycles & roadmap sync", "Team BNS · Cycle 14", "LN"),
        DeveloperIntegration("jira", "Jira", IntegrationCategory.WORKFLOW, "Sprints, epics & engineering tickets", "Project BANS", "JR"),
        DeveloperIntegration("notion", "Notion", IntegrationCategory.WORKFLOW, "Specs, PRDs & team knowledge base", "Product Wiki", "NT"),
        DeveloperIntegration("slack", "Slack", IntegrationCategory.WORKFLOW, "Deploy alerts & engineering channels", "#bansify-builds", "SL"),

        // Auth, Pay & Ops (4)
        DeveloperIntegration("stripe", "Stripe", IntegrationCategory.TOOLS, "Checkout, subscriptions & webhooks", "Test mode · INR/USD", "ST"),
        DeveloperIntegration("clerk", "Clerk", IntegrationCategory.TOOLS, "Drop-in auth, passkeys & sessions", "bansify.clerk.dev", "CK"),
        DeveloperIntegration("sentry", "Sentry", IntegrationCategory.TOOLS, "Error tracking, traces & release health", "android-compose", "SN"),
        DeveloperIntegration("posthog", "PostHog", IntegrationCategory.TOOLS, "Product analytics, flags & replays", "bansify-mobile", "PH")
    )
}
