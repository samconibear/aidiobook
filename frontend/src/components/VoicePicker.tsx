import { useRef } from 'react';
import type { VoiceDto } from '../api/client';

const SAMPLES_BASE = import.meta.env.VITE_VOICE_SAMPLES_BASE_URL ?? '';

interface Props {
  voices: VoiceDto[];
  selected: VoiceDto | null;
  onSelect: (voice: VoiceDto) => void;
}

export function VoicePicker({ voices, selected, onSelect }: Props) {
  const audioRef = useRef<HTMLAudioElement | null>(null);

  function playSample(voice: VoiceDto, e: React.MouseEvent) {
    e.stopPropagation();
    if (!SAMPLES_BASE) return;
    const url = `${SAMPLES_BASE}/${voice.id}/${voice.engine}.mp3`;
    if (audioRef.current) {
      audioRef.current.pause();
    }
    const audio = new Audio(url);
    audioRef.current = audio;
    audio.play().catch(() => {});
  }

  if (!voices.length) return <p className="muted">Loading voices…</p>;

  return (
    <div className="voice-grid">
      {voices.map(v => (
        <button
          key={`${v.id}-${v.engine}`}
          type="button"
          className={`voice-card${selected?.id === v.id && selected?.engine === v.engine ? ' selected' : ''}`}
          onClick={() => onSelect(v)}
        >
          <span className="voice-name">{v.name}</span>
          <span className="voice-meta">{v.gender} · {v.engine}</span>
          {SAMPLES_BASE && (
            <span className="play-btn" onClick={e => playSample(v, e)}>▶</span>
          )}
        </button>
      ))}
    </div>
  );
}
