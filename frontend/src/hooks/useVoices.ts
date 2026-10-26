import { useEffect, useState } from 'react';
import { getVoices } from '../api/client';
import type { VoiceDto } from '../api/client';

export function useVoices() {
  const [voices, setVoices] = useState<VoiceDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getVoices()
      .then(({ voices }) => setVoices(voices.filter(v => v.languageCode.startsWith('en-'))))
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return { voices, loading, error };
}
