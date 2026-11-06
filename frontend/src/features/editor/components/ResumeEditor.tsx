import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { templates as initialTemplates } from '../../../data/templateCatalog';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import type { Resume, Template } from '../../../types';
import { useTemplates } from '../../templates/hooks/useTemplates';
import { usePdfExport } from '../hooks/usePdfExport';
import { useResumeDraft } from '../hooks/useResumeDraft';
import type { EditorModule } from '../model/editorModules';
import { DraftRecoveryDialog } from './DraftRecoveryDialog';
import { EditorPreview } from './EditorPreview';
import { EditorToolbar } from './EditorToolbar';
import { ExportDialog } from './ExportDialog';
import { ModuleNavigation } from './ModuleNavigation';
import { ResumeForm } from './ResumeForm';
import { TemplateSwitchDialog } from './TemplateSwitchDialog';

export function ResumeEditor({ initialResume }: { initialResume: Resume }) {
  const user = useSession((state) => state.user)!;
  const navigate = useNavigate();
  const { data: templateData } = useTemplates();
  const templates = templateData || initialTemplates;
  const draft = useResumeDraft(initialResume, user.id);
  const pdf = usePdfExport(draft.save, templates, user.member);
  const [module, setModule] = useState<EditorModule>('basic');
  const [switchingTemplate, setSwitchingTemplate] = useState(false);
  const template = templates.find((item) => item.id === draft.resume.templateId) || templates[0];

  async function save(goBack = false) {
    try {
      await draft.save();
      if (goBack) navigate('/me');
    } catch (error) {
      notify((error as Error).message);
    }
  }
  function selectTemplate(nextTemplate: Template) {
    if (nextTemplate.isVip && !user.member) {
      navigate('/pricing');
      notify('开通会员，解锁更多原创模板');
      return;
    }
    draft.change({ ...draft.resume, templateId: nextTemplate.id });
    setSwitchingTemplate(false);
  }

  return (
    <main className="editor-page">
      <EditorToolbar
        title={draft.resume.title}
        status={draft.status}
        error={draft.error}
        exporting={pdf.exporting}
        onTitleChange={(title) => draft.change({ ...draft.resume, title })}
        onBack={() => void save(true)}
        onSave={() => void save()}
        onSwitchTemplate={() => setSwitchingTemplate(true)}
        onExport={() => void pdf.exportPdf()}
      />
      <div className="editor-workspace">
        <ModuleNavigation selected={module} onSelect={setModule} />
        <ResumeForm
          module={module}
          content={draft.resume.content}
          error={draft.error}
          onChange={(content) => draft.change({ ...draft.resume, content })}
          onRetrySave={() => void save()}
        />
        <EditorPreview
          content={draft.resume.content}
          template={template}
          watermark={!user.member}
        />
      </div>
      {draft.localDraft && (
        <DraftRecoveryDialog
          draft={draft.localDraft}
          serverVersion={draft.serverVersion}
          onDismiss={draft.dismissDraft}
          onDiscard={draft.discardDraft}
          onRestore={draft.restoreDraft}
        />
      )}
      {switchingTemplate && (
        <TemplateSwitchDialog
          content={draft.resume.content}
          templates={templates}
          selectedId={template.id}
          onSelect={selectTemplate}
          onClose={() => setSwitchingTemplate(false)}
        />
      )}
      {pdf.document && <ExportDialog {...pdf.document} onClose={pdf.closePreview} />}
    </main>
  );
}
