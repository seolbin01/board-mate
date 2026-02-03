interface StreamingMessageProps {
  content: string;
}

export default function StreamingMessage({ content }: StreamingMessageProps) {
  return (
    <div className="flex justify-start mb-4">
      <div className="max-w-[80%]">
        <div className="px-4 py-3 rounded-2xl bg-stone-100 text-stone-800 rounded-bl-md border border-stone-200">
          <div className="text-sm leading-relaxed whitespace-pre-wrap break-words">
            {content}
            <span className="inline-block w-2 h-4 bg-stone-400 ml-1 animate-pulse" />
          </div>
        </div>
        <div className="text-xs text-stone-400 mt-1 text-left">
          입력 중...
        </div>
      </div>
    </div>
  );
}
