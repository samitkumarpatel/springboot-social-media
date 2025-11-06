import { FormEvent, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, Comment, PostSummary, Reply } from '../api'
import { useAuth } from '../auth/AuthContext'

export default function PostList() {
  const [posts, setPosts] = useState<PostSummary[]>([])
  const [postComments, setPostComments] = useState<Record<number, Comment[]>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const { userId } = useAuth()

  // UI-only filters
  const [authorFilter, setAuthorFilter] = useState<'all' | 'me'>('all')
  const [monthFilter, setMonthFilter] = useState<'all' | string>('all')

  useEffect(() => {
    let mounted = true
    setLoading(true)
    api.listPosts()
      .then(async data => {
        if (!mounted) return
        setPosts(data)
        // Load comments for each post to show full feed
        const entries = await Promise.all(data.map(async p => [p.id, await api.listComments(p.id)] as const))
        if (mounted) {
          const map: Record<number, Comment[]> = {}
          for (const [id, cs] of entries) map[id] = cs
          setPostComments(map)
        }
      })
      .catch(e => { if (mounted) setError(String(e)) })
      .finally(() => { if (mounted) setLoading(false) })
    return () => { mounted = false }
  }, [])

  const monthOptions = useMemo(() => {
    const set = new Set<string>()
    for (const p of posts) {
      const d = new Date(p.createdAt)
      const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
      set.add(key)
    }
    return Array.from(set).sort().reverse()
  }, [posts])

  const filtered = useMemo(() => {
    return posts.filter(p => {
      if (authorFilter === 'me' && userId != null && p.authorId !== userId) return false
      if (monthFilter !== 'all') {
        const d = new Date(p.createdAt)
        const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
        if (key !== monthFilter) return false
      }
      return true
    })
  }, [posts, authorFilter, monthFilter, userId])

  const sorted = useMemo(() => filtered.slice().sort((a, b) => b.id - a.id), [filtered])

  async function onCreate(e: FormEvent) {
    e.preventDefault()
    if (!title.trim() || !content.trim()) return
    if (userId == null) return
    try {
      const created = await api.createPost({ title, content, authorId: userId })
      setPosts(prev => [{
        id: created.id,
        title: created.title,
        content: created.content,
        authorId: created.authorId,
        createdAt: created.createdAt,
        likeCount: created.likeCount ?? 0,
        commentCount: 0
      }, ...prev])
      setPostComments(prev => ({ ...prev, [created.id]: [] }))
      setTitle('')
      setContent('')
    } catch (e) {
      setError(String(e))
    }
  }

  return (
    <div>
      <section className="card mb-4">
        <h3 className="text-lg font-semibold mb-2">Create Post</h3>
        <form onSubmit={onCreate} className="flex flex-col gap-3">
          <input className="input" placeholder="What's on your mind? (Title)" value={title} onChange={e => setTitle(e.target.value)} />
          <textarea className="textarea" placeholder="Share something..." value={content} onChange={e => setContent(e.target.value)} />
          <div className="flex items-center justify-between">
            <span className="muted">Posting as user {userId ?? '—'}</span>
            <button type="submit" className="btn btn-primary">Post</button>
          </div>
        </form>
      </section>

      <section className="card mb-4">
        <div className="flex flex-wrap gap-3 items-center">
          <div className="flex items-center gap-2">
            <label className="muted">Author</label>
            <select className="input py-2" value={authorFilter} onChange={e => setAuthorFilter(e.target.value as 'all' | 'me')}>
              <option value="all">All</option>
              <option value="me">My posts</option>
            </select>
          </div>
          <div className="flex items-center gap-2">
            <label className="muted">Month</label>
            <select className="input py-2" value={monthFilter} onChange={e => setMonthFilter(e.target.value as any)}>
              <option value="all">All</option>
              {monthOptions.map(m => (
                <option key={m} value={m}>{m}</option>
              ))}
            </select>
          </div>
          <button className="btn btn-ghost btn-sm" onClick={() => { setAuthorFilter('all'); setMonthFilter('all') }}>Clear</button>
        </div>
      </section>

      {loading && <div className="card">Loading...</div>}
      {error && <div className="card" style={{ color: 'crimson' }}>{error}</div>}

      {sorted.map((p, i) => (
        <FeedPost key={p.id} index={i} post={p} comments={postComments[p.id] || []} onReloadComments={async () => {
          const cs = await api.listComments(p.id)
          setPostComments(prev => ({ ...prev, [p.id]: cs }))
        }} />
      ))}
    </div>
  )
}

function FeedPost({ index, post, comments, onReloadComments }: { index: number; post: PostSummary; comments: Comment[]; onReloadComments: () => Promise<void> }) {
  const { userId } = useAuth()
  const [likeCount, setLikeCount] = useState(post.likeCount)
  const [commentText, setCommentText] = useState('')
  const [showComments, setShowComments] = useState((comments?.length ?? 0) > 0)

  function countNestedReplies(list: Reply[] | undefined): number {
    if (!list || list.length === 0) return 0
    let total = 0
    for (const r of list) {
      total += 1 + countNestedReplies(r.childReplies)
    }
    return total
  }

  const commentsCount = comments?.length ?? 0
  const repliesCount = (comments ?? []).reduce((sum, c) => sum + countNestedReplies(c.replies), 0)
  const totalDiscussion = commentsCount + repliesCount

  async function likePost() {
    if (userId == null) return
    const res = await api.likePost(post.id, userId)
    setLikeCount(res.likeCount)
  }

  async function addComment(e: FormEvent) {
    e.preventDefault()
    if (!commentText.trim() || userId == null) return
    await api.createComment(post.id, { authorId: userId, content: commentText })
    setCommentText('')
    await onReloadComments()
  }

  const bg = index % 2 === 0 ? 'bg-white dark:bg-gray-900' : 'bg-gray-50 dark:bg-gray-950'
  return (
    <article className={`card mb-4 ${bg}`}>
      <h3 className="text-xl font-semibold">{post.title}</h3>
      <p className="muted">Post #{post.id} • by user {post.authorId} • {new Date(post.createdAt).toLocaleString()}</p>
      <p className="mt-2 whitespace-pre-wrap">{post.content}</p>
      <div className="mt-3 flex items-center gap-3">
        <button onClick={likePost} className="btn btn-ghost">👍 Like</button>
        <span className="muted">{likeCount} likes</span>
        <button onClick={() => setShowComments(v => !v)} className="btn btn-ghost btn-sm">
          💬 {showComments ? 'Hide' : 'Show'} comments ({totalDiscussion})
        </button>
        <Link to={`/posts/${post.id}`} className="btn btn-ghost" aria-label="More options" title="Open details">⋯</Link>
      </div>

      {showComments && (
        <>
          <form onSubmit={addComment} className="mt-3 flex items-center gap-2">
            <input className="input" placeholder="Write a comment..." value={commentText} onChange={e => setCommentText(e.target.value)} />
            <button type="submit" className="btn btn-primary">Comment</button>
          </form>

          <div className="mt-3 space-y-2">
            {comments.map((c, ci) => (
              <FeedComment key={c.id} index={ci} postId={post.id} comment={c} onChanged={onReloadComments} />
            ))}
          </div>
        </>
      )}
    </article>
  )
}

function FeedComment({ index, postId, comment, onChanged }: { index: number; postId: number; comment: Comment; onChanged: () => Promise<void> }) {
  const { userId } = useAuth()
  const [replyText, setReplyText] = useState('')
  const [likeCount, setLikeCount] = useState(comment.likeCount ?? 0)
  const [showReplies, setShowReplies] = useState((comment.replies?.length ?? 0) > 0)

  function countNested(list: Reply[] | undefined): number {
    if (!list || list.length === 0) return 0
    let total = 0
    for (const r of list) total += 1 + countNested(r.childReplies)
    return total
  }
  const totalReplies = countNested(comment.replies)

  async function likeComment() {
    if (userId == null) return
    const res = await api.likeComment(postId, comment.id, userId)
    setLikeCount(res.likeCount)
  }

  async function addReply(e: FormEvent) {
    e.preventDefault()
    if (!replyText.trim() || userId == null) return
    await api.createReplyToComment(postId, comment.id, { authorId: userId, content: replyText })
    setReplyText('')
    await onChanged()
  }

  const bg = index % 2 === 0 ? 'bg-transparent' : 'bg-gray-50 dark:bg-gray-900'
  return (
    <div className={`mt-3 rounded-md px-3 py-2 ${bg}`}>
      <div className="flex items-center justify-between">
        <strong>Comment #{comment.id} by user {comment.authorId}</strong>
        <span className="muted">{new Date(comment.createdAt).toLocaleString()}</span>
      </div>
      <p className="mt-1">{comment.content}</p>
      <div className="mt-1 flex items-center gap-2">
        <button onClick={likeComment} className="btn btn-ghost">👍 Like</button>
        <span className="muted">{likeCount} likes</span>
      </div>

      <div className="mt-2">
        <button onClick={() => setShowReplies(v => !v)} className="btn btn-ghost btn-sm">💬 {showReplies ? 'Hide' : 'Show'} replies ({totalReplies})</button>
      </div>

      {showReplies && (
        <>
          <form onSubmit={addReply} className="mt-2 flex items-center gap-3">
            <input className="input-underline-sm" placeholder="Write a reply..." value={replyText} onChange={e => setReplyText(e.target.value)} />
            <button type="submit" className="btn btn-primary btn-sm">Reply</button>
          </form>

          <div className="mt-2 ml-6 space-y-2">
            {(comment.replies ?? []).map((r, ri) => (
              <FeedReply key={r.id} index={ri} postId={postId} commentId={comment.id} reply={r} onChanged={onChanged} />
            ))}
          </div>
        </>
      )}
    </div>
  )
}

function FeedReply({ index, postId, commentId, reply, onChanged }: { index: number; postId: number; commentId: number; reply: Reply; onChanged: () => Promise<void> }) {
  const { userId } = useAuth()
  const [likeCount, setLikeCount] = useState(reply.likeCount ?? 0)
  const [replyText, setReplyText] = useState('')
  const [showChildren, setShowChildren] = useState(false)

  function countNested(list: Reply[] | undefined): number {
    if (!list || list.length === 0) return 0
    let total = 0
    for (const r of list) total += 1 + countNested(r.childReplies)
    return total
  }
  const childCount = countNested(reply.childReplies)

  async function likeReply() {
    if (userId == null) return
    const res = await api.likeReply(postId, commentId, reply.id, userId)
    setLikeCount(res.likeCount)
  }

  async function addReplyToReply(e: React.FormEvent) {
    e.preventDefault()
    if (!replyText.trim() || userId == null) return
    await api.createReplyToReply(postId, commentId, reply.id, { authorId: userId, content: replyText })
    setReplyText('')
    await onChanged()
  }

  const bg = index % 2 === 0 ? 'bg-transparent' : 'bg-gray-100 dark:bg-gray-800'
  return (
    <div className={`rounded-md px-3 py-2 ${bg}`}>
      <div className="flex items-center justify-between">
        <strong>Reply #{reply.id} by user {reply.authorId}</strong>
        <span className="muted">{new Date(reply.createdAt).toLocaleString()}</span>
      </div>
      <p className="mt-1">{reply.content}</p>
      <div className="mt-1 flex items-center gap-2">
        <button onClick={likeReply} className="btn btn-ghost">👍 Like</button>
        <span className="muted">{likeCount} likes</span>
      </div>

      <div className="mt-2">
        <button onClick={() => setShowChildren(v => !v)} className="btn btn-ghost btn-sm">💬 {showChildren ? 'Hide' : 'Show'} replies ({childCount})</button>
      </div>

      {showChildren && (
        <>
          <form onSubmit={addReplyToReply} className="mt-2 flex items-center gap-3">
            <input className="input-underline-sm" placeholder="Write a reply..." value={replyText} onChange={e => setReplyText(e.target.value)} />
            <button type="submit" className="btn btn-primary btn-sm">Reply</button>
          </form>

          {(reply.childReplies ?? []).length > 0 && (
            <div className="mt-2 ml-6 space-y-2">
              {(reply.childReplies ?? []).map((cr, cri) => (
                <FeedReply key={cr.id} index={cri} postId={postId} commentId={commentId} reply={cr} onChanged={onChanged} />
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}


