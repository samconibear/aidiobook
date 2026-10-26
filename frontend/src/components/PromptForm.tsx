import { useState } from 'react';
import { postGenerate } from '../api/client';
import type { Length, Style, VoiceDto } from '../api/client';
import { VoicePicker } from './VoicePicker';
import { useVoices } from '../hooks/useVoices';

interface Props {
  onJobStarted: (jobId: string) => void;
}

export function PromptForm({ onJobStarted }: Props) {
  const { voices, loading: voicesLoading, error: voicesError } = useVoices();
  const [prompt, setPrompt] = useState('');
  const [length, setLength] = useState<Length>('SHORT');
  const [style, setStyle] = useState<Style>('DOCUMENTARY');
  const [voice, setVoice] = useState<VoiceDto | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!voice) { setError('Please select a voice.'); return; }
    if (!prompt.trim()) { setError('Please enter a prompt.'); return; }
    setError(null);
    setSubmitting(true);
    try {
      const { jobId } = await postGenerate({ prompt, length, style, voiceId: voice.id, engine: voice.engine });
      onJobStarted(jobId);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="prompt-form" onSubmit={handleSubmit}>
      <h1>Audiobook Generator</h1>

      <label>
        Prompt
        <textarea
          value={prompt}
          onChange={e => setPrompt(e.target.value)}
          placeholder="e.g. The history of the Roman Empire"
          rows={4}
          required
        />
      </label>

      <div className="row">
        <label>
          Length
          <select value={length} onChange={e => setLength(e.target.value as Length)}>
            <option value="SHORT">Short (~5 min)</option>
            <option value="MEDIUM">Medium (~15 min)</option>
            <option value="LONG">Long (~30 min)</option>
          </select>
        </label>

        <label>
          Style
          <select value={style} onChange={e => setStyle(e.target.value as Style)}>
            <option value="DOCUMENTARY">Documentary</option>
            <option value="NARRATIVE">Narrative</option>
            <option value="EDUCATIONAL">Educational</option>
          </select>
        </label>
      </div>

      <label>Voice</label>
      {voicesError && <p className="error">Failed to load voices: {voicesError}</p>}
      {voicesLoading
        ? <p className="muted">Loading voices…</p>
        : <VoicePicker voices={voices} selected={voice} onSelect={setVoice} />
      }

      {error && <p className="error">{error}</p>}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Generating…' : 'Generate Audiobook'}
      </button>
    </form>
  );
}
