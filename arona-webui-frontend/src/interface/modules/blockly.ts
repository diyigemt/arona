export interface BlocklyProject {
  name: string;
  blocklyProject: string | BlocklyProjectWorkspace;
}

export interface BlocklyProjectWorkspace {
  [key: string]: string;
}
