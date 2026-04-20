const fs = require('fs');
const content = fs.readFileSync('src/components/modals/CrearEventoModal.jsx', 'utf8');

const tags = [];
const regex = /<\/?([a-zA-Z0-9]+)[^>]*>/g;
let match;
const lines = content.split('\n');

function getLineNumber(index) {
  let chars = 0;
  for (let i = 0; i < lines.length; i++) {
    chars += lines[i].length + 1; // +1 for \n
    if (chars > index) return i + 1;
  }
}

while ((match = regex.exec(content)) !== null) {
  const tagStr = match[0];
  const tagName = match[1];
  
  if (tagStr.endsWith('/>')) continue; // self-closing
  // skip self-closing common tags in React if written without /> like <input> <img>
  if (tagStr.match(/<(input|img|br|hr|meta|link)[^>]*>/i) && !tagStr.includes('</')) continue;
  
  if (tagStr.startsWith('</')) {
    const last = tags.pop();
    if (!last || last.name !== tagName) {
      console.log(`Mismatch! Expected ${last ? last.name : 'none'} but found </${tagName}> at line ${getLineNumber(match.index)}`);
      break;
    }
  } else {
    tags.push({name: tagName, line: getLineNumber(match.index)});
  }
}

if (tags.length > 0) {
  console.log('Unclosed tags:', tags);
} else {
  console.log('All matched!');
}
