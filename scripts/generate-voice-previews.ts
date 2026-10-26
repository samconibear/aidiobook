import { PollyClient, DescribeVoicesCommand, SynthesizeSpeechCommand, Engine, OutputFormat } from '@aws-sdk/client-polly';
import { S3Client, PutObjectCommand } from '@aws-sdk/client-s3';

const BUCKET = process.env.AUDIO_SAMPLES_BUCKET;
if (!BUCKET) {
  console.error('AUDIO_SAMPLES_BUCKET env var is required');
  process.exit(1);
}

const REGION = process.env.AWS_REGION ?? 'eu-west-2';
const polly = new PollyClient({ region: REGION });
const s3 = new S3Client({ region: REGION });

const ENGINES: Engine[] = ['standard', 'neural', 'long-form', 'generative'];

const FILLER_SENTENCES = [
  "I love helping people discover the power of the spoken word.",
  "There's nothing quite like settling in for a good story.",
  "Every great journey begins with a single sentence.",
  "Words have a way of painting pictures in your mind.",
  "Let me take you on an adventure through sound and story.",
  "The world is full of fascinating tales just waiting to be told.",
  "I believe every book deserves to be heard, not just read.",
  "Close your eyes and let your imagination do the rest.",
  "A great narrator makes even the ordinary feel extraordinary.",
  "From the first word to the last, I'll keep you company.",
];

async function getEnglishVoices(): Promise<{ id: string; name: string; engine: Engine }[]> {
  const voices: { id: string; name: string; engine: Engine }[] = [];
  for (const engine of ENGINES) {
    const res = await polly.send(new DescribeVoicesCommand({ Engine: engine }));
    for (const v of res.Voices ?? []) {
      if (v.LanguageCode?.startsWith('en-') && v.Id && v.Name) {
        voices.push({ id: v.Id, name: v.Name, engine });
      }
    }
  }
  return voices;
}

function sampleText(name: string, index: number): string {
  const filler = FILLER_SENTENCES[index % FILLER_SENTENCES.length];
  return `Hello, my name is ${name}. ${filler} I hope you enjoy listening as much as I enjoy speaking.`;
}

async function synthesize(text: string, voiceId: string, engine: Engine): Promise<Uint8Array> {
  const res = await polly.send(new SynthesizeSpeechCommand({
    Text: text,
    VoiceId: voiceId as any,
    Engine: engine,
    OutputFormat: 'mp3' as OutputFormat,
  }));
  const chunks: Uint8Array[] = [];
  for await (const chunk of res.AudioStream as AsyncIterable<Uint8Array>) {
    chunks.push(chunk);
  }
  const total = chunks.reduce((n, c) => n + c.length, 0);
  const out = new Uint8Array(total);
  let offset = 0;
  for (const chunk of chunks) {
    out.set(chunk, offset);
    offset += chunk.length;
  }
  return out;
}

async function upload(key: string, audio: Uint8Array): Promise<void> {
  await s3.send(new PutObjectCommand({
    Bucket: BUCKET,
    Key: key,
    Body: audio,
    ContentType: 'audio/mpeg',
  }));
}

async function main() {
  const voices = await getEnglishVoices();
  console.log(`Found ${voices.length} English voice/engine combinations`);

  let done = 0;
  for (let i = 0; i < voices.length; i++) {
    const { id, name, engine } = voices[i];
    const key = `previews/${id}-${engine}.mp3`;
    const text = sampleText(name, i);
    try {
      const audio = await synthesize(text, id, engine);
      await upload(key, audio);
      done++;
      console.log(`[${done}/${voices.length}] ${key}`);
    } catch (err) {
      console.error(`SKIP ${id} (${engine}): ${(err as Error).message}`);
    }
  }

  console.log(`\nDone: ${done}/${voices.length} previews uploaded to s3://${BUCKET}/previews/`);
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
