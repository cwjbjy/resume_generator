import type { PDFDocumentLoadingTask } from 'pdfjs-dist';

interface RenderCallbacks {
  onPageCount: (count: number) => void;
  onPageRendered: () => void;
  onError: (error: Error) => void;
}

/** Starts rendering and returns cleanup for the worker and any unfinished pages. */
export function renderPdfPages(url: string, container: HTMLDivElement, callbacks: RenderCallbacks) {
  const state: { active: boolean; task?: PDFDocumentLoadingTask } = { active: true };
  container.replaceChildren();
  async function render() {
    try {
      const [pdfjs, worker] = await Promise.all([
        import('pdfjs-dist'),
        import('pdfjs-dist/build/pdf.worker.min.mjs?url'),
      ]);
      if (!state.active) return;
      pdfjs.GlobalWorkerOptions.workerSrc = worker.default;
      state.task = pdfjs.getDocument(url);
      const document = await state.task.promise;
      if (!state.active) return;
      callbacks.onPageCount(document.numPages);
      for (let pageNumber = 1; pageNumber <= document.numPages; pageNumber++) {
        const page = await document.getPage(pageNumber);
        if (!state.active) return;
        const viewport = page.getViewport({ scale: 1.6 });
        const canvas = window.document.createElement('canvas');
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        canvas.setAttribute('role', 'img');
        canvas.setAttribute('aria-label', `PDF 第 ${pageNumber} 页，共 ${document.numPages} 页`);
        await page.render({ canvas, viewport }).promise;
        if (!state.active) return;
        const wrapper = window.document.createElement('div');
        wrapper.className = 'pdf-rendered-page';
        const caption = window.document.createElement('p');
        caption.textContent = `${pageNumber} / ${document.numPages}`;
        wrapper.append(canvas, caption);
        container.append(wrapper);
        callbacks.onPageRendered();
      }
    } catch (error) {
      if (state.active)
        callbacks.onError(error instanceof Error ? error : new Error('预览暂时无法加载'));
    }
  }
  void render();
  return () => {
    state.active = false;
    void state.task?.destroy();
  };
}
