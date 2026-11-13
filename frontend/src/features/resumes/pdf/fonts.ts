import { Font } from '@react-pdf/renderer';
export function registerPdfFonts(base = '/fonts') {
  Font.register({
    family: 'NotoSansSC',
    fonts: [
      { src: `${base}/NotoSansSC-Regular.ttf`, fontWeight: 400 },
      { src: `${base}/NotoSansSC-Bold.ttf`, fontWeight: 700 },
    ],
  });
}
if (typeof window !== 'undefined') registerPdfFonts();
Font.registerHyphenationCallback((word) => word.split(/(\u200a)/g).filter(Boolean));
