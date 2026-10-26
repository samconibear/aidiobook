import { useEffect, useRef, useState } from 'react';
import { getJob } from '../api/client';
import type { JobResponse } from '../api/client';

const POLL_INTERVAL_MS = 3000;
const TERMINAL = new Set(['COMPLETE', 'FAILED']);

export function useJobPoller(jobId: string | null) {
  const [job, setJob] = useState<JobResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!jobId) return;

    let cancelled = false;

    async function poll() {
      try {
        const result = await getJob(jobId!);
        if (cancelled) return;
        setJob(result);
        if (!TERMINAL.has(result.status)) {
          timerRef.current = setTimeout(poll, POLL_INTERVAL_MS);
        }
      } catch (e: unknown) {
        if (!cancelled) setError(e instanceof Error ? e.message : String(e));
      }
    }

    poll();

    return () => {
      cancelled = true;
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [jobId]);

  return { job, error };
}
