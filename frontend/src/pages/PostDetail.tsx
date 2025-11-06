import { FormEvent, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, Comment, Post, Reply } from '../api'
import { useAuth } from '../auth/AuthContext'

export default function PostDetail() {
  const { id } = useParams()
  const postId = Number(id)

  const [post, setPost] = useState<Post | null>(null)
  const [comments, setComments] = useState<Comment[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const [commentContent, setCommentContent] = useState('')
  const { userId } = useAuth()
  const [showComments, setShowComments] = useState(true)

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

  useEffect(() => {
    let mounted = true
    async function load() {
      try {
        const [p, cs] = await Promise.all([
          api.getPost(postId),
          api.listComments(postId)
        ])
        if (mounted) {
          setPost(p)
          setComments(cs)
        }
      } catch (e) {
        if (mounted) setError(String(e))
      } finally {
        if (mounted) setLoading(false)
      }
    }
    load()
    return () => { mounted = false }
  }, [postId])

  async function toggleLike(userId: number) {
    try {
      const res = await api.likePost(postId, userId)
      setPost(prev => prev ? { ...prev, likeCount: res.likeCount } : prev)
    } catch (e) { setError(String(e)) }
  }

  async function addComment(e: FormEvent) {
    e.preventDefault()
    if (!commentContent.trim()) return
    try {
      if (userId == null) return
      const c = await api.createComment(postId, { authorId: userId, content: commentContent })
      setComments(prev => [c, ...prev])
      setCommentContent('')
    } catch (e) { setError(String(e)) }
  }

  async function likeComment(commentId: number, userId: number) {
    try {
      const res = await api.likeComment(postId, commentId, userId)
      setComments(prev => prev.map(c => c.id === commentId ? { ...c, likeCount: res.likeCount } : c))
    } catch (e) { setError(String(e)) }
  }

  async function addReplyToComment(commentId: number, authorId: number, content: string) {
    const r = await api.createReplyToComment(postId, commentId, { authorId, content })
    setComments(prev => prev.map(c => c.id === commentId ? { ...c, replies: [r, ...(c.replies ?? [])] } : c))
  }

  async function likeReply(commentId: number, replyId: number, userId: number) {
    try {
      const res = await api.likeReply(postId, commentId, replyId, userId)
      setComments(prev => prev.map(c =>
        c.id !== commentId ? c : {
          ...c,
          replies: (c.replies ?? []).map(r => r.id === replyId ? { ...r, likeCount: res.likeCount } : r)
        }
      ))
    } catch (e) { setError(String(e)) }
  }

  if (loading) return <div className="card">Loading...</div>
  if (error) return <div className="card text-red-600">{error}</div>
  if (!post) return <div className="card">Not found</div>

  return (
    <div>
      <div className="mb-4">
        <Link to="/" className="btn btn-ghost" aria-label="Back">
          ← Back
        </Link>
      </div>
      <article className="card">
        <h2 className="text-2xl font-bold">{post.title}</h2>
        <p className="muted">Post #{post.id} • by user {post.authorId} • {new Date(post.createdAt).toLocaleString()}</p>
        <p className="mt-2 whitespace-pre-wrap">{post.content}</p>
        <div className="mt-3 flex items-center gap-3">
          <button className="btn btn-ghost" onClick={() => userId != null && toggleLike(userId)}>👍 Like</button>
          <span className="muted">{post.likeCount ?? 0} likes</span>
          <button onClick={() => setShowComments(v => !v)} className="btn btn-ghost btn-sm">💬 {showComments ? 'Hide' : 'Show'} comments ({totalDiscussion})</button>
        </div>
      </article>

      {showComments && (
        <section className="card mt-4">
          <h3 className="text-lg font-semibold">Add a comment</h3>
          <form onSubmit={addComment} className="mt-2 flex items-center gap-2">
            <textarea className="textarea" placeholder="Say something..." value={commentContent} onChange={e => setCommentContent(e.target.value)} />
            <button type="submit" className="btn btn-primary">Comment</button>
          </form>
        </section>
      )}

      {showComments && (
        <section>
          {comments.map((c, ci) => (
            <CommentItem key={c.id}
              index={ci}
              comment={c}
              onLike={(userId) => likeComment(c.id, userId)}
              onReply={(authorId, content) => addReplyToComment(c.id, authorId, content)}
              onLikeReply={(replyId, userId) => likeReply(c.id, replyId, userId)}
            />
          ))}
        </section>
      )}
    </div>
  )
}

function CommentItem({ index, comment, onLike, onReply, onLikeReply }: {
  index: number
  comment: Comment
  onLike: (userId: number) => void
  onReply: (authorId: number, content: string) => void
  onLikeReply: (replyId: number, userId: number) => void
}) {
  const { userId } = useAuth()
  const [content, setContent] = useState('')
  const [showReplies, setShowReplies] = useState((comment.replies?.length ?? 0) > 0)
  function countNested(list: Reply[] | undefined): number {
    if (!list || list.length === 0) return 0
    let total = 0
    for (const r of list) total += 1 + countNested(r.childReplies)
    return total
  }
  const repliesTotal = countNested(comment.replies)

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (!content.trim() || userId == null) return
    await onReply(userId, content)
    setContent('')
  }

  const bg = index % 2 === 0 ? 'bg-transparent' : 'bg-gray-50 dark:bg-gray-900'
  return (
    <article className={`card ${bg}`}>
      <h4 style={{ marginTop: 0 }}>Comment #{comment.id} by user {comment.authorId}</h4>
      <p className="muted">{new Date(comment.createdAt).toLocaleString()}</p>
      <p>{comment.content}</p>
      <div className="row" style={{ gap: 8 }}>
        <button onClick={() => userId != null && onLike(userId)}>👍 Like</button>
        <span className="muted">{comment.likeCount ?? 0} likes</span>
      </div>

      <div className="mt-2">
        <button onClick={() => setShowReplies(v => !v)} className="btn btn-ghost btn-sm">💬 {showReplies ? 'Hide' : 'Show'} replies ({repliesTotal})</button>
      </div>

      {showReplies && (
        <>
          <form onSubmit={submit} className="mt-3 flex items-center gap-3">
            <input className="input-underline-sm" placeholder="Reply..." value={content} onChange={e => setContent(e.target.value)} />
            <button type="submit" className="btn btn-primary btn-sm">Reply</button>
          </form>

          {(comment.replies ?? []).map((r, ri) => (
            <ReplyItem key={r.id} index={ri} reply={r} onLike={(userId) => onLikeReply(r.id, userId)} />
          ))}
        </>
      )}
    </article>
  )
}

function ReplyItem({ index, reply, onLike }: { index: number; reply: Reply; onLike: (userId: number) => void }) {
  const { userId } = useAuth()
  const [replyText, setReplyText] = useState('')
  const [showChildren, setShowChildren] = useState(false)
  function countNested(list: Reply[] | undefined): number {
    if (!list || list.length === 0) return 0
    let total = 0
    for (const r of list) total += 1 + countNested(r.childReplies)
    return total
  }
  const childCount = countNested(reply.childReplies)
  const bg = index % 2 === 0 ? 'bg-transparent' : 'bg-gray-100 dark:bg-gray-800'
  return (
    <div className={`ml-6 rounded-md px-3 py-2 ${bg}`}>
      <div className="flex items-center justify-between">
        <strong>Reply #{reply.id} by user {reply.authorId}</strong>
        <span className="muted">{new Date(reply.createdAt).toLocaleString()}</span>
      </div>
      <p className="mt-2">{reply.content}</p>
      <div className="flex items-center gap-2">
        <button onClick={() => userId != null && onLike(userId)} className="btn btn-ghost">👍 Like</button>
        <span className="muted">{reply.likeCount ?? 0} likes</span>
      </div>

      <div className="mt-2">
        <button onClick={() => setShowChildren(v => !v)} className="btn btn-ghost btn-sm">💬 {showChildren ? 'Hide' : 'Show'} replies ({childCount})</button>
      </div>

      {showChildren && (
        <>
          <form onSubmit={(e) => { e.preventDefault(); if (!replyText.trim() || userId == null) return; }} className="mt-2 flex items-center gap-3">
            <input className="input-underline-sm" placeholder="Write a reply..." value={replyText} onChange={e => setReplyText(e.target.value)} />
            <button type="submit" className="btn btn-primary btn-sm" disabled>{'Reply (use Home feed)'}</button>
          </form>

          {(reply.childReplies ?? []).length > 0 && (
            <div className="mt-2 ml-6 space-y-2">
              {(reply.childReplies ?? []).map((cr, cri) => (
                <div key={cr.id} className={`${cri % 2 === 0 ? 'bg-transparent' : 'bg-gray-100 dark:bg-gray-800'} rounded-md px-3 py-2`}>
                  <div className="flex items-center justify-between">
                    <strong>Reply #{cr.id} by user {cr.authorId}</strong>
                    <span className="muted">{new Date(cr.createdAt).toLocaleString()}</span>
                  </div>
                  <p className="mt-2">{cr.content}</p>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}


