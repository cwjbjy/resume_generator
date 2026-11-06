import { Link } from 'react-router-dom';
import { editorModules, type EditorModule } from '../model/editorModules';

export function ModuleNavigation({
  selected,
  onSelect,
}: {
  selected: EditorModule;
  onSelect: (module: EditorModule) => void;
}) {
  return (
    <aside className="editor-modules">
      {editorModules.map((module) => (
        <button
          className={selected === module.key ? 'active' : ''}
          key={module.key}
          onClick={() => onSelect(module.key)}
        >
          <module.icon size={20} />
          <span>{module.label}</span>
        </button>
      ))}
      <Link className="editor-vip" to="/pricing">
        <span>PRO</span>升级会员
      </Link>
    </aside>
  );
}
