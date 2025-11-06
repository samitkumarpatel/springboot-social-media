import { apiUrl } from './config'
export type PostSummary = {
  id: number
  title: string
  content: string
  authorId: number
  createdAt: string
  likeCount: number
  commentCount: number
}

export type Post = {
  id: number
  title: string
  content: string
  authorId: number
  createdAt: string
  updatedAt?: string
  likeCount?: number
}

export type Comment = {
  id: number
  authorId: number
  content: string
  createdAt: string
  updatedAt?: string
  likeCount?: number
  replies?: Reply[]
}

export type Reply = {
  id: number
  authorId: number
  content: string
  createdAt: string
  updatedAt?: string
  likeCount?: number
  childReplies?: Reply[]
}

export type LikeResponse = { liked: boolean; likeCount: number }

async function http<T>(input: string, init?: RequestInit): Promise<T> {
  const res = await fetch(apiUrl(input), {
    headers: { 'Content-Type': 'application/json' },
    ...init
  })
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`)
  return res.status === 204 ? (undefined as unknown as T) : res.json()
}

export const api = {
  listPosts: () => http<PostSummary[]>('/api/posts'),
  getPost: (id: number) => http<Post>(`/api/posts/${id}`),
  createPost: (data: { title: string; content: string; authorId: number }) =>
    http<Post>('/api/posts', { method: 'POST', body: JSON.stringify(data) }),
  updatePost: (id: number, data: { title: string; content: string }) =>
    http<Post>(`/api/posts/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deletePost: (id: number) => http<void>(`/api/posts/${id}`, { method: 'DELETE' }),
  likePost: (id: number, userId: number) =>
    http<LikeResponse>(`/api/posts/${id}/like?userId=${userId}`, { method: 'POST' }),

  listComments: (postId: number) => http<Comment[]>(`/api/posts/${postId}/comments`),
  createComment: (postId: number, data: { authorId: number; content: string }) =>
    http<Comment>(`/api/posts/${postId}/comments`, { method: 'POST', body: JSON.stringify(data) }),
  likeComment: (postId: number, commentId: number, userId: number) =>
    http<LikeResponse>(`/api/posts/${postId}/comments/${commentId}/like?userId=${userId}`, { method: 'POST' }),

  listReplies: (postId: number, commentId: number) =>
    http<Reply[]>(`/api/posts/${postId}/comments/${commentId}/replies`),
  createReplyToComment: (postId: number, commentId: number, data: { authorId: number; content: string }) =>
    http<Reply>(`/api/posts/${postId}/comments/${commentId}/replies`, { method: 'POST', body: JSON.stringify(data) }),
  createReplyToReply: (postId: number, commentId: number, replyId: number, data: { authorId: number; content: string }) =>
    http<Reply>(`/api/posts/${postId}/comments/${commentId}/replies/${replyId}/replies`, { method: 'POST', body: JSON.stringify(data) }),
  likeReply: (postId: number, commentId: number, replyId: number, userId: number) =>
    http<LikeResponse>(`/api/posts/${postId}/comments/${commentId}/replies/${replyId}/like?userId=${userId}`, { method: 'POST' })
}


