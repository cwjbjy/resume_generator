import { PdfPages } from './PdfPages';
export default function PdfViewer({ url }: { url: string }) {
  return <PdfPages key={url} url={url} />;
}
