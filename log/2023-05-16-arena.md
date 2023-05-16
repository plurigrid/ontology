# Arena: Local-first Graph-based Data Store with CRDT Synchronization

## 1. Data Model

Arena implements two core structures for holding the graph's nodes (vertices) and edges (connections between vertices) using the following schema:

```sql
CREATE TABLE nodes (
  id INTEGER PRIMARY KEY,
  label TEXT,
  properties JSON
);

CREATE TABLE edges (
  id INTEGER PRIMARY KEY,
  source_node_id INTEGER,
  target_node_id INTEGER,
  label TEXT,
  properties JSON,
  FOREIGN KEY(source_node_id) REFERENCES nodes(id),
  FOREIGN KEY(target_node_id) REFERENCES nodes(id)
);
```

These tables store graph data and enable efficient querying of connections between vertices.

## 2. Programming Language and Infrastructure

- Arena is written in Rust for its exceptional performance, safety, and compatibility with WASM.
- DuckDB, along with the `duckdb-rs` (Rust API) library, provides an efficient and flexible data store.
- Yrs is used for implementing CRDTs.
- Rust-libp2p is utilized for peer-to-peer networking, although not natively compatible with WASM.

## 3. WebAssembly (WASM)

- Rust code is migrated to WASM for optimal portability and interopreability.

## 4. Conflict Resolution and Synchronization

- Yrs in Rust is leveraged for CRDT synchronization with eventual consistency.
- Algorithms are designed for merging data in case of updates, maintaining consistent results.

## 5. Connectivity and P2P API Layer

- The rust-libp2p library is employed for secure data transfer and messaging between distributed instances.
- APIs are designed for secure, flexible, and multi-database communications.

## 6. Querying the Data Store

- Arena can be queried as a typical SQL database or traversed as a graph.
- An application-level API or minimal domain-specific language is developed to query, interact, and manipulate the graph-based data stored in DuckDB.

## 7. Real-time Editing and Synchronization

- Users can edit the contents of a node in real-time, with changes reflected to all peers through the CRDT.
- The entire database topology (changing the connections between nodes) can be edited and synchronized.

## 8. Performance, Scalability, and Optimization

- Continuous testing and evaluation of CRDTs optimize operation schematics and inter-layer function development.
- Efficient data retrieval and connectivity are prioritized within the database's architecture, ensuring long-term scalability.
- Modifications to the relationship implementation are applied to improve performance across distributed users.

Arena's formal specification focuses on defining, implementing, optimizing, and testing components that drive development workflow alternatives and components. By addressing all contingencies, the system guarantees dynamic responses, flexible reactions to changing demand workloads, and readiness for production in user-specific application requests.
