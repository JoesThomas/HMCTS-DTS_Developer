import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './style.css';

type Status = 'TODO' | 'IN_PROGRESS' | 'DONE';
type Task = {
  id: number;
  title: string;
  description: string | null;
  status: Status;
  dueAt: string;
};

const labels: Record<Status, string> = {
  TODO: 'To do',
  IN_PROGRESS: 'In progress',
  DONE: 'Done',
};

/**
 * Shared API helper: normalise HTTP errors into messages suitable for the UI.
 * DELETE returns 204 without a response body, so it must not be parsed as JSON.
 */
async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  });

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      message = body.detail || body.message || message;
    } catch {
      // Keep the HTTP status message if the server did not return JSON.
    }
    throw new Error(message);
  }

  return response.status === 204 ? (undefined as T) : ((await response.json()) as T);
}

function App() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [due, setDue] = useState('');
  const [status, setStatus] = useState<Status>('TODO');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  // Refresh from the API after mutations so the server remains the source of truth.
  async function refresh() {
    setTasks(await request<Task[]>('/api/tasks'));
  }

  useEffect(() => {
    refresh().catch((e: Error) => setError(e.message));
  }, []);

  async function create(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      // datetime-local is entered in local time; the API expects an offset-aware date.
      await request('/api/tasks', {
        method: 'POST',
        body: JSON.stringify({
          title,
          description,
          status,
          dueAt: new Date(due).toISOString(),
        }),
      });
      setTitle('');
      setDescription('');
      setDue('');
      setStatus('TODO');
      await refresh();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      // Always re-enable the submit button, including after network errors.
      setBusy(false);
    }
  }

  async function change(id: number, nextStatus: Status) {
    setError('');
    try {
      await request(`/api/tasks/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status: nextStatus }),
      });
      await refresh();
    } catch (e) {
      setError((e as Error).message);
    }
  }

  async function remove(id: number) {
    // Require confirmation because deletion cannot be undone in this demo.
    if (!confirm('Delete this task?')) return;
    setError('');
    try {
      await request(`/api/tasks/${id}`, { method: 'DELETE' });
      await refresh();
    } catch (e) {
      setError((e as Error).message);
    }
  }

  return (
    <main>
      <header>
        <p className="eyebrow">HMCTS technical challenge · Demonstration</p>
        <h1>Caseworker task manager</h1>
        <p>Organise work, track progress and keep deadlines visible.</p>
      </header>
      <section className="panel">
        <h2>Create a task</h2>
        <form onSubmit={create}>
          <label>
            Title *
            <input maxLength={150} required value={title} onChange={e => setTitle(e.target.value)} placeholder="e.g. Review case documents" />
          </label>
          <label>
            Description
            <textarea maxLength={2000} value={description} onChange={e => setDescription(e.target.value)} placeholder="Optional notes" />
          </label>
          <div className="two">
            <label>
              Due date and time *
              <input type="datetime-local" required value={due} onChange={e => setDue(e.target.value)} />
            </label>
            <label>
              Status
              <select value={status} onChange={e => setStatus(e.target.value as Status)}>
                {Object.entries(labels).map(([key, value]) => <option key={key} value={key}>{value}</option>)}
              </select>
            </label>
          </div>
          <button disabled={busy}>{busy ? 'Saving…' : 'Add task'}</button>
        </form>
      </section>
      {error && <p className="error" role="alert">{error}</p>}
      <section className="panel">
        <div className="heading"><h2>Tasks</h2><span>{tasks.length} total</span></div>
        {tasks.length === 0 ? (
          <p>No tasks yet. Create one above.</p>
        ) : (
          <div className="tasks">
            {/* Sort a copy: React state should not be mutated during rendering. */}
            {[...tasks].sort((a, b) => a.dueAt.localeCompare(b.dueAt)).map(task => (
              <article key={task.id}>
                <div>
                  <h3>{task.title}</h3>
                  {task.description && <p>{task.description}</p>}
                  <small>Due {new Date(task.dueAt).toLocaleString()}</small>
                </div>
                <div className="actions">
                  <label>
                    <span className="sr-only">Status for {task.title}</span>
                    <select aria-label={`Status for ${task.title}`} value={task.status} onChange={e => change(task.id, e.target.value as Status)}>
                      {Object.entries(labels).map(([key, value]) => <option key={key} value={key}>{value}</option>)}
                    </select>
                  </label>
                  <button className="danger" onClick={() => remove(task.id)}>Delete</button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')!).render(<App />);
