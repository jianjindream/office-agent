import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'
import { createHighlighterCore } from 'shiki/core'
import { createJavaScriptRegexEngine } from 'shiki/engine/javascript'

const highlighterPromise = createHighlighterCore({
  themes: [import('@shikijs/themes/github-light'), import('@shikijs/themes/github-dark')],
  langs: [
    import('@shikijs/langs/javascript'),
    import('@shikijs/langs/typescript'),
    import('@shikijs/langs/java'),
    import('@shikijs/langs/json'),
    import('@shikijs/langs/bash'),
    import('@shikijs/langs/python'),
    import('@shikijs/langs/sql'),
    import('@shikijs/langs/html'),
    import('@shikijs/langs/css'),
    import('@shikijs/langs/markdown'),
  ],
  engine: createJavaScriptRegexEngine(),
})

const normalizeLanguage = (language: string) => {
  const aliases: Record<string, string> = {
    js: 'javascript',
    ts: 'typescript',
    sh: 'bash',
    shell: 'bash',
    py: 'python',
    md: 'markdown',
  }
  return aliases[language] ?? language
}

export async function renderMarkdown(source: string): Promise<string> {
  const highlighter = await highlighterPromise
  const md = new MarkdownIt({
    html: false,
    linkify: true,
    typographer: true,
    breaks: true,
    highlight(code, language) {
      const lang = normalizeLanguage(language || 'text')
      const loaded = highlighter.getLoadedLanguages()
      const safeLang = loaded.includes(lang as never) ? lang : 'text'
      const highlighted = highlighter.codeToHtml(code, {
        lang: safeLang,
        themes: { light: 'github-light', dark: 'github-dark' },
      })
      return `<div class="code-block"><button type="button" class="code-copy">复制</button>${highlighted}</div>`
    },
  })

  const defaultLinkOpen = md.renderer.rules.link_open
  md.renderer.rules.link_open = (tokens, index, options, env, self) => {
    tokens[index].attrSet('target', '_blank')
    tokens[index].attrSet('rel', 'noopener noreferrer')
    return defaultLinkOpen ? defaultLinkOpen(tokens, index, options, env, self) : self.renderToken(tokens, index, options)
  }

  return DOMPurify.sanitize(md.render(source), {
    ADD_ATTR: ['target'],
  })
}
