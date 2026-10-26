import { useState } from 'react';
import { PromptForm } from './components/PromptForm';
import { JobStatus } from './components/JobStatus';
import { AudioResult } from './components/AudioResult';
import { useJobPoller } from './hooks/useJobPoller';

type AppState = 'idle' | 'polling' | 'complete' | 'failed';

export default function App() {
  const [appState, setAppState] = useState<AppState>('idle');
  const [jobId, setJobId] = useState<string | null>(null);

  const { job, error: pollError } = useJobPoller(appState === 'polling' ? jobId : null);

  if (appState === 'polling' && job) {
    if (job.status === 'COMPLETE') setAppState('complete');
    else if (job.status === 'FAILED') setAppState('failed');
  }

  function handleJobStarted(id: string) {
    setJobId(id);
    setAppState('polling');
  }

  function handleReset() {
    setJobId(null);
    setAppState('idle');
  }

  return (
    <main className="container">
      {appState === 'idle' && <PromptForm onJobStarted={handleJobStarted} />}

      {appState === 'polling' && <JobStatus job={job} />}

      {appState === 'complete' && job && (
        <AudioResult job={job} onReset={handleReset} />
      )}

      {appState === 'failed' && (
        <div className="job-status">
          <p className="error">Generation failed: {job?.error ?? pollError ?? 'Unknown error'}</p>
          <button type="button" onClick={handleReset}>Try again</button>
        </div>
      )}
    </main>
  );
}
