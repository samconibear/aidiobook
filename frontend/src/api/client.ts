const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export type Length = 'SHORT' | 'MEDIUM' | 'LONG';
export type Style = 'DOCUMENTARY' | 'NARRATIVE' | 'EDUCATIONAL';
export type JobStatus = 'QUEUED' | 'WRITING_SCRIPT' | 'GENERATING_AUDIO' | 'COMPLETE' | 'FAILED';

export interface VoiceDto {
  id: string;
  name: string;
  gender: string;
  engine: string;
  languageCode: string;
}

export interface GenerateRequest {
  prompt: string;
  length: Length;
  style: Style;
  voiceId: string;
  engine: string;
}

export interface JobResponse {
  jobId: string;
  status: JobStatus;
  stage: JobStatus;
  script: string | null;
  audioUrl: string | null;
  error: string | null;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, init);
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.json() as Promise<T>;
}

export function getVoices(): Promise<{ voices: VoiceDto[] }> {
  return request('/voices');
}

export function postGenerate(body: GenerateRequest): Promise<{ jobId: string }> {
  return request('/generate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
}

export function getJob(jobId: string): Promise<JobResponse> {
  return request(`/jobs/${jobId}`);
}
