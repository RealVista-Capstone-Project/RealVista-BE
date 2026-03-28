# Thiết kế UI quản lí agent, hợp đồng, đánh giá

**Session ID:** ses_2f5e201c6ffeD4olYHkEJfqSFh
**Created:** 3/20/2026, 2:20:31 PM
**Updated:** 3/20/2026, 2:29:36 PM

---

## User

hiện tại thì tôi đang cần thiết kế đi màn như thé nào cho hiệu quả khi mà tôi là 1 owner vào trong màn quản lí các agent đang thuế và có thế kết thúc hợp đồng, hoặc hủy hợp đồng hiện tại sau đó thì có thể đánh giá,(rating và viết review cho agent), như vậy có ổn không và tôi cần flow đơn giản cho usecase này nhưng gần với thực tế nhất 

---

## Assistant (Plan · claude-opus-4.6 · 28.8s)



Tôi sẽ khám phá codebase trước để hiểu tech stack và các component/page liên quan hiện tại, sau đó đưa ra thiết kế flow phù hợp.

_Thinking:_

The user is asking about designing a screen/flow for an owner to manage their rented agents. Let me understand the requirements:

1. Owner views a list of agents they're currently renting
2. Owner can end a contract
3. Owner can cancel a contract
4. Owner can rate and review an agent (after ending/canceling)

They want a simple but realistic flow for this use case.

Let me first explore the codebase to understand the current project structure, tech stack, and any existing related components before providing design recommendations.

**Tool: task**

**Input:**
```json
{
  "description": "Explore codebase structure",
  "prompt": "Explore this codebase thoroughly to understand:\n\n1. What tech stack is being used (React/Next.js/Vue, UI library, state management, etc.)\n2. What existing pages/screens exist - look at the routing structure\n3. Any existing agent-related, contract-related, or booking-related components/pages\n4. Any existing review/rating components\n5. The project's folder structure pattern (how pages, components, services, types are organized)\n6. Any existing API services or types related to agents, contracts, owners\n\nReturn a comprehensive summary of findings including:\n- Tech stack details\n- Folder structure pattern\n- Existing relevant pages/components\n- Existing types/interfaces related to agents, contracts, reviews\n- API service patterns used in the project",
  "subagent_type": "explore"
}
```

**Error:**
```
Tool execution aborted
```

---

## User

bạn tập trung vào feature @src\features\agent-engagement/ á 

---

## Assistant (Plan · claude-opus-4.6 · 166.1s)



_Thinking:_

The user wants me to focus on the `src/features/agent-engagement/` directory to understand the existing code, then help design a flow for an owner managing their hired agents with the ability to:
1. View agents currently under contract
2. End/terminate contracts
3. Cancel contracts
4. Rate and review agents after contract ends

Let me explore that specific feature directory first.

**Tool: task**

**Input:**
```json
{
  "description": "Explore agent-engagement feature",
  "prompt": "Thoroughly explore the directory `src/features/agent-engagement/` and its surroundings. I need to understand:\n\n1. Complete folder structure of `src/features/agent-engagement/` - every file and subfolder\n2. Read ALL files in this directory to understand existing components, types, services, hooks\n3. Look at routing - how is this feature routed? Check `src/app/` or router config for agent-engagement routes\n4. Read any types/interfaces related to agents, contracts, engagements\n5. Check `src/features/` for other related features (like agent profiles, reviews, etc.)\n6. Look at the overall `src/features/` directory to see what features exist\n7. Check for any existing rating/review components in the project\n8. Check the UI component library being used (shadcn, MUI, ant design, etc.) - look at `src/components/ui/` or similar\n\nReturn a COMPREHENSIVE summary including:\n- Complete file tree of agent-engagement feature\n- Key types/interfaces (copy them verbatim)\n- Existing components and what they do\n- API services and endpoints\n- Routing structure\n- UI library being used\n- Any existing patterns for list pages, detail pages, modals, forms",
  "subagent_type": "explore"
}
```

**Output:**
```
task_id: ses_2f5e0e409ffe8O46gZ5XJiIHWD (for resuming to continue this task if needed)

<task_result>
I now have a complete picture. Here is the comprehensive summary:

---

## COMPREHENSIVE EXPLORATION REPORT: `src/features/agent-engagement/`

---

### 1. COMPLETE FILE TREE

```
src/features/agent-engagement/
├── hooks/
│   └── use-hired-agents.ts
├── i18n/
│   ├── en.json
│   └── vi.json
├── lib/
│   └── utils.ts
├── model/
│   └── manage-agent-context.tsx
└── ui/
    ├── agent-detail-panel.tsx
    └── agent-list-item.tsx

src/entities/agent-engagement/       (entity layer - types, API, queries)
├── api/
│   ├── agent-engagement.api.ts
│   ├── agent-engagement.queries.ts
│   ├── index.ts
│   └── keys.ts
├── model/
│   └── types.ts
└── index.ts

src/screens/manage-agent/            (screen layer - page composition)
├── i18n/
│   ├── en.json
│   └── vi.json
├── ui/
│   └── manage-agent-page.tsx
└── index.ts

app/[locale]/manage-agent/           (Next.js App Router route)
├── layout.tsx
└── page.tsx
```

---

### 2. KEY TYPES/INTERFACES (VERBATIM)

**`src/entities/agent-engagement/model/types.ts`:**
```ts
export interface AgentEngagement {
  agent_user_id: string;
  agent_full_name: string;
  agent_avatar_url: string | null;
  agent_phone: string | null;
  agent_email: string | null;
  agent_bio: string | null;
  agent_specialties: string[] | string | null;
  agent_service_areas: string[] | string | null;
  agent_rating: number | null;
  agent_years_of_experience: number | null;
  agent_properties_sold: number | null;
  property_id: string;
  property_address: string | null;
  property_type_name: string | null;
  property_location_name: string | null;
  engagement_id: string;
  engagement_type: string;
  status: string;
  hired_at: string;
}

export interface AgentEngagementPageResponse {
  content: AgentEngagement[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
  first: boolean;
  last: boolean;
}

export interface GetAgentEngagementsParams {
  page?: number;
  size?: number;
  status?: string;
  search?: string;
}
```

**`src/shared/types/api-response.ts`:**
```ts
export type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
};
```

**Engagement statuses used in UI:** `ACTIVE`, `PENDING`, `COMPLETED`, `CANCELLED`, `ACCEPTED`, `REJECTED`, `EXPIRED`

---

### 3. EXISTING COMPONENTS AND WHAT THEY DO

| Component | File | Purpose |
|---|---|---|
| **`AgentDetailPanel`** | `features/agent-engagement/ui/agent-detail-panel.tsx` | Right-side slide-in panel showing full agent details: avatar, star rating, status badge, contact info (email/phone/service areas), professional info (experience/properties sold/engagement type/hired date), specialties, bio, and assigned property info. Has a **"Add Review" button** with a `// TODO: implement add review logic` comment. |
| **`AgentListItem`** | `features/agent-engagement/ui/agent-list-item.tsx` | Row item in a 12-column grid list showing: hired date, agent avatar+name+email, star rating, property type+location, status badge, and a hover-reveal "more" button. Highlights when selected. |
| **`ManageAgentProvider` / `useManageAgentContext`** | `features/agent-engagement/model/manage-agent-context.tsx` | React Context provider managing all list state: search query, status filter, pagination (page/size), selected agent, and the data fetch via `useHiredAgentsQuery`. Resets page to 1 on filter changes. `ITEMS_PER_PAGE = 10`. |
| **`ManageAgentPage`** | `screens/manage-agent/ui/manage-agent-page.tsx` | Full page composition: wraps content in `ManageAgentProvider`. Contains filter bar (status Select + search Input), column header, list of `AgentListItem`s, pagination controls, and conditionally renders `AgentDetailPanel` on the right when an agent is selected. |

---

### 4. API SERVICES AND ENDPOINTS

**API function** (`src/entities/agent-engagement/api/agent-engagement.api.ts`):
```ts
export const agentEngagementApi = {
  getHiredAgents: (params: GetAgentEngagementsParams = {}) =>
    http.get<ApiResponse<AgentEngagementPageResponse>>(buildUrl(params)),
} as const;
```
- **Endpoint:** `GET /engagements/hired-agents?page=&size=&status=&search=`
- Uses the shared `http` client which auto-attaches Bearer token from NextAuth session.

**Query factory** (`src/entities/agent-engagement/api/agent-engagement.queries.ts`):
```ts
export const agentEngagementQueries = {
  list: (params) => queryOptions({
    queryKey: agentEngagementKeys.list(params),
    queryFn: () => agentEngagementApi.getHiredAgents(params),
    staleTime: 2 * 60 * 1000,  // 2 minutes
  }),
} as const;
```

**Query keys** (`src/entities/agent-engagement/api/keys.ts`):
```ts
export const agentEngagementKeys = {
  all: ['agent-engagements'] as const,
  lists: () => [...agentEngagementKeys.all, 'list'] as const,
  list: (params) => [...agentEngagementKeys.lists(), params] as const,
} as const;
```

**Hook** (`src/features/agent-engagement/hooks/use-hired-agents.ts`):
```ts
export function useHiredAgentsQuery(params: GetAgentEngagementsParams = {}) {
  return useQuery(agentEngagementQueries.list(params));
}
```

**Response shape:** The API returns `{ status, payload }` where `payload` is `ApiResponse<AgentEngagementPageResponse>`, so the data path is: `data.payload.data.content` (array of `AgentEngagement`).

---

### 5. ROUTING STRUCTURE

```
app/[locale]/manage-agent/
├── layout.tsx    -> RoleGuard(allowedRoles=['owner']) + DashboardLayout
└── page.tsx      -> imports ManageAgentPage from @/screens/manage-agent
```

- **Route:** `/[locale]/manage-agent` (e.g. `/en/manage-agent`, `/vi/manage-agent`)
- **Access control:** `RoleGuard` restricts to `owner` role only. Non-owners are redirected to `/`.
- **Layout:** Uses `DashboardLayout` widget with a sidebar containing a single "Manage Agents" link.
- **Role mapping:** Backend `OWNER` role maps to frontend `owner` role via `src/shared/lib/auth/rbac.ts`.
- **Post-login redirect:** Owners are redirected to `/manage-agent` after login.
- **Route constant:** `ROUTES.manageAgent.root = '/manage-agent'`

**i18n registration** (`src/shared/config/i18n/request.ts`):
- Both `@/features/agent-engagement/i18n/${locale}.json` and `@/screens/manage-agent/i18n/${locale}.json` are imported into the global message bundle.

---

### 6. UI LIBRARY

**Custom shadcn/ui + Radix + Tailwind CSS stack:**

- **Primitives:** Radix UI (`@radix-ui/react-dialog`, `react-avatar`, `react-checkbox`, `react-label`, `react-popover`, `react-select`, `react-separator`, `react-slider`, `react-slot`, `react-switch`, `react-tooltip`)
- **Styling:** Tailwind CSS v4 + `class-variance-authority` (cva) + `tailwind-merge` + `clsx`
- **Icons:** `lucide-react`
- **Forms:** `react-hook-form` + `@hookform/resolvers` + `zod`
- **Data fetching:** `@tanstack/react-query`
- **State management:** `zustand`
- **i18n:** `next-intl`
- **Animation:** `framer-motion` + `tw-animate-css`
- **Toast:** `sonner`
- **Charts:** `recharts`

**Available shared UI components** (at `src/shared/ui/`):
Avatar, Badge, Button, Calendar, Card, Chart, Checkbox, Dialog, Field, Form, Input, InputGroup, Label, Map, Popover, Realvista-Button, Realvista-Input-Date-Picker, Realvista-Listing-Card, Realvista-Pagination, Realvista-Property-Listing-Search-Bar, Select, Separator, Sheet, Sidebar, Skeleton, Slider, Sonner (toast), Switch, Textarea, Tooltip, Top-Nav, Profile-Dropdown, Login-Required-Modal, Property-Filters, Property-Filters-Modal, Property-Map, Property-Map-Marker, Price-Range-Slider, Inline-Advanced-Filters, Share-Icons, Chart-Line-Dot, Attribute-Icon, Property-Search-Header

---

### 7. ALL FEATURES IN `src/features/`

| Feature | Description |
|---|---|
| `agent-engagement/` | Agent hiring management (list, detail, context) |
| `auth/` | Authentication flows |
| `bookmark/` | Property bookmarking |
| `chat-listing-card/` | Chat listing card component |
| `chat/` | Real-time chat (WebSocket) |
| `home/` | Homepage feature |
| `listing/` | Listing feature |
| `monthly-cost-breakdown/` | Monthly cost breakdown |
| `price-and-tour/` | Pricing info and tour booking |
| `property-about/` | Property about section |
| `property-gallery/` | Property image gallery |
| `property-header/` | Property header section |
| `property-search/` | Property search feature |
| `rent-price-history/` | Rent price history charts |
| `rental-features/` | Rental features display |
| `search/` | General search |
| `tenant-application/` | Tenant application submission |

---

### 8. EXISTING RATING/REVIEW COMPONENTS

**There are NO existing rating/review components or entities.** The only references to "review" or "rating" in the codebase are:

1. **`agent-detail-panel.tsx` line 261:** `// TODO: implement add review logic` - The "Add Review" button exists but has no implementation.
2. **`agent_rating`** field on `AgentEngagement` - displayed as stars (read-only) in both `AgentDetailPanel` and `AgentListItem`.
3. **`formatRating()`** utility in `features/agent-engagement/lib/utils.ts` - a helper that formats a number or null to string.
4. **`price-and-tour/ui/book-tour-modal.tsx`** has a `handleReview` and "Review Booking" button (unrelated to agent reviews - it's for reviewing a tour booking before submission).

There is no `review` entity, no review API service, no review form, no review types anywhere in the codebase.

---

### 9. ESTABLISHED PATTERNS

**Architecture: Feature-Sliced Design (FSD)**
```
app/          -> Next.js App Router (routes only, delegates to screens)
src/screens/  -> Page-level composition (assembles features into pages)
src/features/ -> Feature modules (UI, hooks, context, i18n)
src/entities/ -> Domain entities (types, API clients, query factories, query keys)
src/widgets/  -> Reusable composed widgets (layouts, modals)
src/shared/   -> Shared infrastructure (ui, lib, types, config)
```

**Pattern: List page with detail panel (master-detail)**
- Context provider holds all state (filters, pagination, selection, data fetching)
- Screen wraps content in provider
- List items are grid-based rows (`grid grid-cols-12`)
- Clicking a row toggles a sticky side panel (`AgentDetailPanel`)
- Pagination is manual prev/next buttons with "Showing X-Y of Z"

**Pattern: Entity layer (types + API + queries + keys)**
```
src/entities/<name>/
├── model/types.ts          -> TypeScript interfaces
├── api/
│   ├── <name>.api.ts       -> HTTP calls using shared http client
│   ├── <name>.queries.ts   -> TanStack Query queryOptions factories
│   ├── keys.ts             -> Query key factory
│   └── index.ts            -> Re-exports
└── index.ts                -> Re-exports all
```

**Pattern: Feature layer**
```
src/features/<name>/
├── ui/                     -> React components ('use client')
├── hooks/                  -> Custom hooks (wrapping entity queries)
├── model/                  -> Context providers, stores
├── lib/                    -> Feature-specific utilities
└── i18n/                   -> en.json, vi.json (registered in shared/config/i18n/request.ts)
```

**Pattern: Modal/form (from `contact-modal` widget)**
- Uses `Dialog`/`DialogContent`/`DialogHeader`/`DialogTitle`/`DialogDescription` from `@/shared/ui/dialog`
- Uses `Form`/`FormField`/`FormItem`/`FormLabel`/`FormControl`/`FormMessage` from `@/shared/ui/form`
- Form state managed via `react-hook-form` + `zod` validation via `@hookform/resolvers`
- Custom hook (`useContactForm`) encapsulates form creation, default values, submission logic
- Zod schema defined in the entity layer (`@/entities/contact`)

**Pattern: HTTP client**
- Shared `http` module at `src/shared/lib/http/http.ts`
- Methods: `http.get<Response>(url)`, `http.post<Response>(url, body)`, `http.put`, `http.delete`, `http.patch`
- Auto-attaches `Authorization: Bearer <token>` on client
- Base URL from environment variable `NEXT_PUBLIC_API_ENDPOINT`
- Returns `{ status: number, payload: Response }`
- Throws `HttpError` (generic) or `EntityError` (422 validation)

**Pattern: i18n**
- `next-intl` with `useTranslations('Namespace')` hook
- Each feature/screen has its own `i18n/en.json` and `i18n/vi.json`
- All message files are merged in `src/shared/config/i18n/request.ts`
- Locales: `en`, `vi`
</task_result>
```

---

## Assistant (Plan · claude-opus-4.6 · 284.1s)

_Thinking:_

Now I have a comprehensive understanding of the codebase. Let me analyze the user's requirements and design a flow for them.

The user wants:
1. Owner goes to manage agents page (already exists)
2. Can see list of hired agents (already exists)
3. Can **end contract** (complete engagement) or **cancel contract** (cancel engagement)
4. After ending/cancelling, can **rate and review** the agent

Let me think about the real-world flow:

In real estate, when an owner hires an agent:
- The contract can be **completed** (work done successfully) → owner should be prompted to review
- The contract can be **cancelled** (terminated early) → owner might still want to leave a review


- The action buttons should adapt based on contract status: ACTIVE contracts show end/cancel options, PENDING contracts allow withdrawal, and COMPLETED/CANCELLED contracts show an option to add a review if one hasn't been left yet. Clicking either action triggers a confirmation dialog, updates the status accordingly, and then surfaces a review modal where the owner can submit a star rating and written feedback.

Tôi đã khám phá kỹ feature `agent-engagement`. Đây là phân tích và đề xuất flow của tôi:

---

## Hiện trạng codebase

**Đã có:**
- Trang danh sách agents thuê (master-detail layout)
- Detail panel bên phải khi click agent
- Nút "Add Review" trong detail panel nhưng chưa implement (`TODO`)
- Các status: `ACTIVE`, `PENDING`, `COMPLETED`, `CANCELLED`, `ACCEPTED`, `REJECTED`, `EXPIRED`

**Chưa có:**
- API/logic kết thúc hoặc hủy hợp đồng
- Entity review/rating (types, API, form, validation)
- Review form/modal

---

## Đề xuất Flow — Owner quản lý Agent

### Flow tổng quan

```
┌─────────────────────────────────────────────────────┐
│  Manage Agents Page (danh sách agent đang thuê)     │
│                                                     │
│  [Agent row] ──click──► [Detail Panel bên phải]     │
│                              │                      │
│                    ┌─────────┴──────────┐           │
│                    │   Status = ACTIVE   │           │
│                    └─────────┬──────────┘           │
│                    ┌─────────┴──────────┐           │
│              [Complete Contract]  [Cancel Contract]  │
│                    │                    │            │
│              Confirm Dialog       Confirm Dialog     │
│              "Hoàn thành?"       "Hủy? Lý do?"      │
│                    │                    │            │
│                    ▼                    ▼            │
│              status → COMPLETED   status → CANCELLED │
│                    │                    │            │
│                    └────────┬───────────┘            │
│                             ▼                        │
│                    ┌────────────────┐                │
│                    │  Review Modal  │                │
│                    │  ⭐⭐⭐⭐⭐      │                │
│                    │  [Write review]│                │
│                    │  [Submit/Skip] │                │
│                    └────────────────┘                │
└─────────────────────────────────────────────────────┘
```

### Chi tiết từng bước

**Bước 1 — Xem agent và chọn hành động**

Owner vào trang → thấy danh sách agents. Click vào 1 agent → detail panel mở ra bên phải. Tùy status hiện tại, hiển thị nút hành động:

| Status | Actions hiển thị |
|---|---|
| `ACTIVE` | **Complete Contract** + **Cancel Contract** |
| `PENDING` | **Cancel** (rút lại trước khi agent accept) |
| `COMPLETED` | **Add Review** (nếu chưa review) |
| `CANCELLED` | **Add Review** (nếu chưa review) |

**Bước 2 — Complete Contract (kết thúc hợp đồng)**

1. Owner click "Complete Contract"
2. **Confirmation Dialog** hiện ra: _"Xác nhận hoàn thành hợp đồng với [Agent Name] cho [Property]?"_
3. Owner xác nhận → API call → status chuyển sang `COMPLETED`
4. **Tự động mở Review Modal** ngay sau khi complete thành công

**Bước 3 — Cancel Contract (hủy hợp đồng)**

1. Owner click "Cancel Contract"
2. **Confirmation Dialog** với textarea: _"Lý do hủy hợp đồng?"_ (bắt buộc nhập lý do)
3. Owner nhập lý do + xác nhận → API call → status chuyển sang `CANCELLED`
4. **Tự động mở Review Modal** (optional — owner có thể skip)

**Bước 4 — Review & Rating**

1. Modal hiện ra với:
   - **Star Rating**: 1-5 sao (bắt buộc)
   - **Review Text**: textarea viết nhận xét (tùy chọn, nhưng encourage)
2. 2 nút: **Submit Review** | **Skip** (bỏ qua)
3. Submit → API call → toast success → đóng modal → refresh list

---

## Câu hỏi cần làm rõ

**Tool: question**

**Input:**
```json
{
  "questions": [
    {
      "question": "Khi Cancel Contract, owner có bắt buộc phải nhập lý do hủy không?",
      "header": "Cancel reason",
      "options": [
        {
          "label": "Bắt buộc nhập lý do (Recommended)",
          "description": "Owner phải giải thích tại sao hủy - realistic hơn, bảo vệ quyền lợi agent"
        },
        {
          "label": "Tùy chọn (optional)",
          "description": "Cho phép hủy mà không cần lý do"
        }
      ]
    },
    {
      "question": "Sau khi Complete/Cancel, Review Modal có nên tự động popup ngay không?",
      "header": "Review timing",
      "options": [
        {
          "label": "Popup ngay sau Complete/Cancel (Recommended)",
          "description": "Tăng tỉ lệ review vì owner đang trong context, giống Grab/Shopee sau khi hoàn thành đơn"
        },
        {
          "label": "Không popup, chỉ hiện nút Add Review",
          "description": "Owner tự click nút Add Review khi muốn, ít intrusive hơn"
        }
      ]
    },
    {
      "question": "Review text (nhận xét viết) có bắt buộc không?",
      "header": "Review text required?",
      "options": [
        {
          "label": "Chỉ rating bắt buộc, text tùy chọn (Recommended)",
          "description": "Giảm friction - owner chỉ cần chấm sao là đủ, viết thêm nếu muốn"
        },
        {
          "label": "Cả rating và text đều bắt buộc",
          "description": "Đảm bảo chất lượng review nhưng có thể khiến owner bỏ qua"
        }
      ]
    },
    {
      "question": "Owner có được review agent mà không cần Complete/Cancel trước không? (VD: agent đã COMPLETED từ trước)",
      "header": "Review independent?",
      "options": [
        {
          "label": "Có, cho review bất cứ khi nào status là COMPLETED/CANCELLED (Recommended)",
          "description": "Linh hoạt - owner có thể quay lại review sau"
        },
        {
          "label": "Không, chỉ review ngay sau khi Complete/Cancel",
          "description": "Đơn giản hơn nhưng ít linh hoạt"
        }
      ]
    }
  ]
}
```

**Output:**
```
User has answered your questions: "Khi Cancel Contract, owner có bắt buộc phải nhập lý do hủy không?"="Bắt buộc nhập lý do (Recommended)", "Sau khi Complete/Cancel, Review Modal có nên tự động popup ngay không?"="Popup ngay sau Complete/Cancel (Recommended)", "Review text (nhận xét viết) có bắt buộc không?"="Chỉ rating bắt buộc, text tùy chọn (Recommended)", "Owner có được review agent mà không cần Complete/Cancel trước không? (VD: agent đã COMPLETED từ trước)"="Có, cho review bất cứ khi nào status là COMPLETED/CANCELLED (Recommended)". You can now continue with the user's answers in mind.
```

---

## Assistant (Plan · claude-opus-4.6 · 34.8s)

_Thinking:_

The user confirmed all the recommended options. Let me now create a comprehensive flow design and implementation plan.

Let me summarize the decisions:
1. Cancel Contract: Bắt buộc nhập lý do
2. Review Modal: Popup ngay sau Complete/Cancel
3. Review text: Chỉ rating bắt buộc, text tùy chọn
4. Review independent: Có, cho review bất cứ khi nào status là COMPLETED/CANCELLED

Now let me design the complete flow and present it clearly.

Tốt, tất cả đều là lựa chọn hợp lý. Đây là flow hoàn chỉnh:

---

## Flow hoàn chỉnh — Owner quản lý Agent Engagement

### State Machine (status transitions)

```
PENDING ──[Cancel]──► CANCELLED
ACTIVE  ──[Complete]──► COMPLETED ──[auto popup]──► Review Modal
ACTIVE  ──[Cancel]──► CANCELLED ──[auto popup]──► Review Modal

COMPLETED (chưa review) ──[Add Review]──► Review Modal
CANCELLED (chưa review) ──[Add Review]──► Review Modal
```

### Action Buttons theo Status

| Status | Buttons trong Detail Panel |
|---|---|
| `PENDING` | `Cancel Request` |
| `ACTIVE` | `Complete Contract` · `Cancel Contract` |
| `COMPLETED` | `Add Review` (ẩn nếu đã review) |
| `CANCELLED` | `Add Review` (ẩn nếu đã review) |
| `REJECTED` / `EXPIRED` | Không có action |

### Luồng chi tiết

**Flow 1 — Complete Contract**
```
[Click "Complete Contract"]
        │
        ▼
┌──────────────────────────┐
│   Confirmation Dialog    │
│                          │
│ "Xác nhận hoàn thành     │
│  hợp đồng với {Agent}   │
│  cho {Property}?"        │
│                          │
│  [Cancel]  [Confirm ✓]  │
└──────────────────────────┘
        │ Confirm
        ▼
   API: PATCH /engagements/{id}/complete
        │ Success
        ▼
   Toast: "Hợp đồng đã hoàn thành"
        │
        ▼ (auto, delay ~500ms)
┌──────────────────────────┐
│     Review Modal         │ ← see Flow 3
└──────────────────────────┘
```

**Flow 2 — Cancel Contract**
```
[Click "Cancel Contract"]
        │
        ▼
┌──────────────────────────┐
│   Cancel Dialog          │
│                          │
│ "Hủy hợp đồng với       │
│  {Agent} cho {Property}" │
│                          │
│ Lý do hủy: *            │
│ ┌──────────────────────┐ │
│ │ textarea (required)  │ │
│ └──────────────────────┘ │
│                          │
│  [Back]  [Confirm Cancel]│ ← nút đỏ/destructive
└──────────────────────────┘
        │ Confirm
        ▼
   API: PATCH /engagements/{id}/cancel
        body: { reason: "..." }
        │ Success
        ▼
   Toast: "Hợp đồng đã bị hủy"
        │
        ▼ (auto, delay ~500ms)
┌──────────────────────────┐
│     Review Modal         │ ← see Flow 3
└──────────────────────────┘
```

**Flow 3 — Review & Rating**
```
┌──────────────────────────────┐
│      Review Modal            │
│                              │
│  Đánh giá {Agent Name}      │
│                              │
│  Rating: ☆☆☆☆☆ *            │ ← click để chọn 1-5, required
│                              │
│  Nhận xét:                   │
│  ┌────────────────────────┐  │
│  │ textarea (optional)    │  │
│  │ "Chia sẻ trải nghiệm  │  │
│  │  của bạn..."           │  │
│  └────────────────────────┘  │
│                              │
│  [Skip]      [Submit Review] │
└──────────────────────────────┘
        │ Submit
        ▼
   API: POST /engagements/{id}/review
        body: { rating: 4, comment: "..." }
        │ Success
        ▼
   Toast: "Cảm ơn! Đánh giá đã được gửi"
   Close modal → Refresh list
```

### Cần thêm vào Types

```ts
// Thêm vào AgentEngagement type
interface AgentEngagement {
  // ... existing fields
  has_review: boolean;         // để biết đã review chưa
  cancellation_reason?: string; // lý do hủy (nếu có)
}

// Entity mới: Review
interface CreateReviewPayload {
  engagement_id: string;
  rating: number;        // 1-5, required
  comment?: string;      // optional
}

// API endpoints mới cần
interface EngagementApiExtension {
  completeEngagement: (id: string) => Promise<...>;
  cancelEngagement: (id: string, reason: string) => Promise<...>;
  submitReview: (payload: CreateReviewPayload) => Promise<...>;
}
```

### Cấu trúc files cần tạo/sửa

```
src/entities/agent-engagement/
├── model/types.ts                  ← EDIT: thêm has_review, CreateReviewPayload
├── api/agent-engagement.api.ts     ← EDIT: thêm complete, cancel, submitReview

src/features/agent-engagement/
├── ui/
│   ├── agent-detail-panel.tsx      ← EDIT: thêm action buttons theo status
│   ├── complete-confirm-dialog.tsx ← NEW: confirmation dialog
│   ├── cancel-contract-dialog.tsx  ← NEW: cancel dialog với textarea lý do
│   ├── review-modal.tsx            ← NEW: rating stars + review text form
│   └── star-rating-input.tsx       ← NEW: interactive star rating component
├── hooks/
│   ├── use-complete-engagement.ts  ← NEW: mutation hook
│   ├── use-cancel-engagement.ts    ← NEW: mutation hook
│   └── use-submit-review.ts        ← NEW: mutation hook
└── i18n/
    ├── en.json                     ← EDIT: thêm translations
    └── vi.json                     ← EDIT: thêm translations
```

---

Flow này gần với thực tế vì:
- **Giống Grab/Shopee**: popup review ngay sau hoàn thành → tỉ lệ review cao
- **Bắt buộc lý do hủy**: bảo vệ agent, có dữ liệu để phân tích
- **Skip được review**: không ép buộc, owner quay lại review sau nếu muốn
- **Rating only cũng OK**: giảm friction tối đa

Bạn muốn tôi tiến hành implement không, hay cần điều chỉnh gì trong flow?

---
