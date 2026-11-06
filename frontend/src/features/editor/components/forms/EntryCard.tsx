import { ArrowDown, ArrowUp, GripVertical, Trash2 } from 'lucide-react';
import type { DragEvent, ReactNode } from 'react';

interface EntryCardProps {
  label: string;
  index: number;
  count: number;
  dragging: boolean;
  children: ReactNode;
  onDragStart: (event: DragEvent<HTMLDivElement>) => void;
  onDragEnd: () => void;
  onDrop: () => void;
  onMove: (destination: number) => void;
  onRemove: () => void;
}

export function EntryCard({
  label,
  index,
  count,
  dragging,
  children,
  onDragStart,
  onDragEnd,
  onDrop,
  onMove,
  onRemove,
}: EntryCardProps) {
  return (
    <div
      className={`entry-card ${dragging ? 'dragging' : ''}`}
      draggable
      onDragStart={onDragStart}
      onDragOver={(event) => event.preventDefault()}
      onDragEnd={onDragEnd}
      onDrop={(event) => {
        event.preventDefault();
        onDrop();
      }}
    >
      <div className="entry-card-header">
        <span>
          <GripVertical size={15} />
          {label} {String(index + 1).padStart(2, '0')}
        </span>
        <div>
          <button
            className="icon-button"
            aria-label="上移条目"
            disabled={index === 0}
            onClick={() => onMove(index - 1)}
          >
            <ArrowUp size={14} />
          </button>
          <button
            className="icon-button"
            aria-label="下移条目"
            disabled={index === count - 1}
            onClick={() => onMove(index + 1)}
          >
            <ArrowDown size={14} />
          </button>
          <button className="icon-button danger" aria-label="删除条目" onClick={onRemove}>
            <Trash2 size={14} />
          </button>
        </div>
      </div>
      {children}
    </div>
  );
}
