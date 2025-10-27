import React, { useMemo } from 'react';
import SpurfyButton from "./SpurfyButton";
import SpurfyAI from "../../assets/SpurfyAI.png";
import { toAbs } from '../../utils/url';
import ReactMarkdown from 'react-markdown';
import remarkBreaks from 'remark-breaks';
import gfm from 'remark-gfm';
import { faAngleRight } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

function MessageBubble({ text, isUser, imageUrl, spaSlug, onGoToSpaDetail, errorMessage }) {
  const bubbleClasses = isUser
    ? "bg-[#67F3EC] rounded-bl-2xl p-3 rounded-tl-2xl rounded-br-2xl self-end"
    : "rounded-br-2xl rounded-bl-2xl rounded-tr-2xl self-start";

  const renderedMarkdown = useMemo(() => {
    if (!text) return null;

    return (
      <ReactMarkdown
        remarkPlugins={[gfm, remarkBreaks]}
        components={{
          p: (props) => <p className="mb-4" {...props} />,
          li: (props) => <li className="list-disc list-inside ml-2 last:mb-4" {...props} />
        }}
      >
        {text}
      </ReactMarkdown>
    );
  }, [text]);

  const resolvedSrc = useMemo(() => toAbs(imageUrl), [imageUrl]);

  if (isUser) {
    return (
      <div className="flex flex-col items-end">
        {/* 이미지: 말풍선 밖 */}
        {resolvedSrc && (
          <div className="w-80 h-80 overflow-hidden rounded-2xl mb-2">
            <img
              src={resolvedSrc}
              alt="사용자가 첨부한 사진"
              className="w-full h-full object-cover"
              loading="lazy"
              onError={(e) => { e.currentTarget.style.display = 'none'; }}
            />
          </div>
        )}

        {/* 텍스트 말풍선 */}
        <div className={`max-w-[80%] flex flex-col ${bubbleClasses} relative`}>
          {text && <div className="whitespace-pre-wrap">{text}</div>}
        </div>
      </div>
    );
  }

  return (
    <div className="flex items-start flex-col gap-4">
      <img src={SpurfyAI} alt="AI Profile" className="w-12 h-12 object-cover flex-shrink-0 rounded-full" />
      <div className={`max-w-[80%] flex flex-col ${bubbleClasses} relative`}>
        {renderedMarkdown}
        {spaSlug && onGoToSpaDetail && !errorMessage && (
          <SpurfyButton
            variant="primary"
            onClick={() => onGoToSpaDetail(spaSlug)}
            className="py-2 px-4 text-sm self-start"
          >
            추천받은 스파 보러가기 <FontAwesomeIcon icon={faAngleRight} className="text-xs ml-4" />
          </SpurfyButton>
        )}
      </div>
    </div>
  );
}

export default React.memo(MessageBubble);