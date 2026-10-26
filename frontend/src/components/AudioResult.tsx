import type { JobResponse } from '../api/client';

interface Props {
  job: JobResponse;
  onReset: () => void;
}

export function AudioResult({ job, onReset }: Props) {
  return (
    <div className="audio-result">
      <h2>Your Audiobook</h2>

      {job.audioUrl && (
        <div className="audio-player">
          <audio controls src={job.audioUrl} />
          <a href={job.audioUrl} download="audiobook.mp3" className="download-link">
            Download MP3
          </a>
        </div>
      )}

      {job.script && (
        <details className="script-section">
          <summary>View script</summary>
          <p className="script">{job.script}</p>
        </details>
      )}

      <button type="button" onClick={onReset}>Generate another</button>
    </div>
  );
}
