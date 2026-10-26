import type { JobResponse, JobStatus as Status } from '../api/client';

const STAGES: Status[] = ['QUEUED', 'WRITING_SCRIPT', 'GENERATING_AUDIO', 'COMPLETE'];

const LABELS: Record<Status, string> = {
  QUEUED: 'Queued',
  WRITING_SCRIPT: 'Writing script',
  GENERATING_AUDIO: 'Generating audio',
  COMPLETE: 'Complete',
  FAILED: 'Failed',
};

interface Props {
  job: JobResponse | null;
}

export function JobStatus({ job }: Props) {
  const currentStage = job?.stage ?? 'QUEUED';
  const currentIdx = STAGES.indexOf(currentStage);

  return (
    <div className="job-status">
      <h2>Generating your audiobook…</h2>
      <ol className="stages">
        {STAGES.map((stage, i) => {
          const done = i < currentIdx || currentStage === 'COMPLETE';
          const active = stage === currentStage && currentStage !== 'COMPLETE';
          return (
            <li key={stage} className={done ? 'done' : active ? 'active' : ''}>
              {LABELS[stage]}
            </li>
          );
        })}
      </ol>
      {job?.status === 'FAILED' && (
        <p className="error">Failed: {job.error ?? 'Unknown error'}</p>
      )}
    </div>
  );
}
